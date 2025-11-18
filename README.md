# Projeto Ondetamoto - Guia de Deploy

Este documento detalha o processo completo para provisionar a infraestrutura na Microsoft Azure, realizar o deploy da aplicação Java (Spring Boot) e verificar a sua funcionalidade.

## DDL da Tabelas

As tabelas são criadas automaticamente pelo Flyway inserido no projeto, caso queira averiguar entre no projeto vá em src > main > resources > db > migration e estarão lá.

## 📝 Descrição da Solução

O projeto **OndeTáMoto?** é uma solução tecnológica baseada em IoT (Internet das Coisas) desenvolvida para a Mottu, uma empresa de motofrete, com o objetivo de gerenciar e controlar motos em tempo real dentro de sua garagem. O sistema utiliza tags inteligentes em cada moto para registrar automaticamente seus movimentos (entrada, saída e permanência). Esses dados são centralizados em um aplicativo mobile com uma interface amigável, permitindo à equipe visualizar o status, localização, e categorização de cada moto.

## 📈 Descrição dos Benefícios para o Negócio

A solução **OndeTáMoto?** resolve o problema de controle ineficiente das motos na garagem da Mottu, substituindo planilhas e anotações manuais. Ela traz os seguintes benefícios para o negócio:

* **Visibilidade e Agilidade**: Oferece informações em tempo real sobre a localização e status das motos, aumentando a visibilidade operacional.
* **Eficiência e Precisão**: Automatiza o registro de movimentações, reduzindo erros humanos e retrabalhos.
* **Organização e Segurança**: Promove um controle mais organizado e seguro da frota.
* **Inovação Adaptada**: Utiliza tecnologia IoT para uma gestão prática e inteligente, sob medida para a operação da Mottu.

## ✔️ Pré-requisitos

Antes de começar, garanta que você tenha:

| Ferramenta / Recurso             | Necessário | Obs.                              |
| -------------------------------- | :--------: | --------------------------------- |
| Conta Azure                      |      ✅     | Necessário para App Service + SQL |
| Azure Cloud Shell ou Azure CLI   |      ✅     | Para criar a infraestrutura       |
| Azure DevOps (Repos + Pipelines) |      ✅     | Usado no deploy                   |
| Gradle                           |      ✅     | Build da aplicação                |
| Java 17                          |      ✅     | Versão configurada no App Service |


## 🎥 Link do Vídeo
[Link do Video de Devops](https://www.youtube.com/watch?v=vp3htHxnF74)

---

## 🚀 Parte 1: Provisionamento da Infraestrutura do Banco de Dados

O primeiro passo é criar os recursos do banco de dados (Grupo de Recursos, Servidor SQL e o próprio Banco de Dados) usando um script no Azure Cloud Shell.

1.  Acesse o [Portal Azure](https://portal.azure.com/) e abra o **Cloud Shell** (ícone `>_` no topo). Certifique-se de que o ambiente selecionado seja o **Bash**.

2.  Crie o script de criação da infraestrutura:
    ```bash
    touch create-sql-server.sh
    chmod +x create-sql-server.sh
    nano create-sql-server.sh
    ```

5.  Cole o seguinte código no editor. **Atenção:** É uma boa prática de segurança não expor senhas diretamente no código. Para ambientes de produção, utilize o Azure Key Vault ou outras formas seguras de gerenciamento de segredos.

    ```bash
    #!/bin/bash
    
    # Variáveis de configuração
    RG="rg-ondetamoto"
    LOCATION="brazilsouth"
    SERVER_NAME="sqlserver-rm557462"
    USERNAME="admsql"
    # Lembre-se da boa prática de não deixar senhas no código em ambientes de produção.
    PASSWORD="Fiap@2tdsvms"
    DBNAME="ondetamotodb"
    
    # Cria o grupo de recursos
    echo "Criando o grupo de recursos: $RG..."
    az group create --name $RG --location $LOCATION
    
    # Cria o servidor SQL
    echo "Criando o servidor SQL: $SERVER_NAME..."
    az sql server create -l $LOCATION -g $RG -n $SERVER_NAME -u $USERNAME -p $PASSWORD --enable-public-network true
    
    # Cria o banco de dados (que estará vazio, pronto para o Flyway)
    echo "Criando o banco de dados: $DBNAME..."
    az sql db create -g $RG -s $SERVER_NAME -n $DBNAME --service-objective Basic --backup-storage-redundancy Local --zone-redundant false
    
    # Cria a regra de firewall para permitir acesso de serviços do Azure e outros IPs
    echo "Configurando a regra de firewall..."
    az sql server firewall-rule create -g $RG -s $SERVER_NAME -n AllowAll --start-ip-address 0.0.0.0 --end-ip-address 255.255.255.255
    
    echo "Infraestrutura do banco de dados criada com sucesso!"
    echo "O banco '$DBNAME' está pronto e vazio para o Flyway gerenciar o schema."
    ```
    Salve e feche o editor (`CTRL + S`, depois `CTRL + X` e `Enter`).

6.  Execute o script para criar os recursos:
    ```bash
    ./create-sql-server.sh
    ```

---

## ⚙️ Parte 2: Deploy da Aplicação com Script Automatizado

Este script irá criar o App Service, o Application Insights e configurar as variáveis de ambiente necessárias para a aplicação se conectar ao banco de dados.

1.  Ainda no Cloud Shell, crie o script de deploy:
    ```bash
    touch deploy-ondetamoto.sh
    chmod +x deploy-ondetamoto.sh
    nano deploy-ondetamoto.sh
    ```

4.  Cole o script abaixo, **lembrando de alterar** o valor da variável `GITHUB_REPO_NAME` para o seu usuário e repositório.

    ```bash
    #!/bin/bash
    # --- Variáveis de Configuração da Aplicação ---
    # Altere 'rm557462' para seu identificador único
    export RESOURCE_GROUP_NAME="rg-ondetamoto"
    export WEBAPP_NAME="ondetamoto-rm557462"
    export APP_SERVICE_PLAN="planOndetamoto"
    export LOCATION="brazilsouth"
    export RUNTIME="JAVA:17-java17"
    
    # --- Variáveis do Banco de Dados ---
    export DB_SERVER_NAME="sqlserver-rm557462"
    export DB_NAME="ondetamotodb"
    export DB_USER="admsql"
    export DB_PASSWORD="Fiap@2tdsvms" # ATENÇÃO: É recomendado usar segredos do Azure DevOps para a senha!
    
    # Construção da URL JDBC dinamicamente
    export JDBC_URL="jdbc:sqlserver://${DB_SERVER_NAME}.database.windows.net:1433;database=${DB_NAME};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
    
    echo "Iniciando a criação da infraestrutura no Azure..."
    
    # Criar o Plano de Serviço do App
    echo "Criando o Plano de Serviço: $APP_SERVICE_PLAN..."
    az appservice plan create \
    --name "$APP_SERVICE_PLAN" \
    --resource-group "$RESOURCE_GROUP_NAME" \
    --location "$LOCATION" \
    --sku F1 \
    --is-linux
    
    # Criar o Serviço de Aplicativo (Web App)
    echo "Criando o Web App: $WEBAPP_NAME..."
    az webapp create \
    --name "$WEBAPP_NAME" \
    --resource-group "$RESOURCE_GROUP_NAME" \
    --plan "$APP_SERVICE_PLAN" \
    --runtime "$RUNTIME"
    
    # Habilita a autenticação Básica (SCM) para permitir o deploy pelo pipeline
    echo "Habilitando credenciais de deploy SCM..."
    az resource update \
    --resource-group "$RESOURCE_GROUP_NAME" \
    --namespace Microsoft.Web \
    --resource-type basicPublishingCredentialsPolicies \
    --name scm \
    --parent sites/"$WEBAPP_NAME" \
    --set properties.allow=true
    
    # Configurar as Variáveis de Ambiente do Banco de Dados na Aplicação
    echo "Configurando as variáveis de ambiente do banco de dados..."
    az webapp config appsettings set \
    --name "$WEBAPP_NAME" \
    --resource-group "$RESOURCE_GROUP_NAME" \
    --settings \
    SPRING_DATASOURCE_USERNAME="$DB_USER" \
    SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD" \
    SPRING_DATASOURCE_URL="$JDBC_URL"
    
    # Reiniciar o Web App para aplicar as configurações
    echo "Reiniciando o Web App para aplicar as novas configurações..."
    az webapp restart \
    --name "$WEBAPP_NAME" \
    --resource-group "$RESOURCE_GROUP_NAME"
    
    echo "Criação e configuração da infraestrutura concluídas com sucesso!"
    ```
    Salve e feche o editor.

5.  Execute o script:
    ```bash
    ./deploy-ondetamoto.sh
    ```
    Este comando irá configurar o GitHub Actions, mas o arquivo de workflow gerado pode precisar de ajustes.

---

## 🔧 Parte 3 — Configurando o CI/CD com Azure Pipelines (Classic)

3.1 Onde o código está?

 - O código fica no Azure Repos, dentro do Azure DevOps.

3.2 Criação da Pipeline Clássica

 - Acesse: Azure DevOps → Pipelines → Create Pipeline

 - Escolha:

   Classic Editor

   Selecione o repositório do projeto no Azure Repos

Escolha o template Java with Gradle (ou pipeline vazia)

-- Etapas configuradas na Pipeline

| Etapa               | Descrição                                       |
| ------------------- | ----------------------------------------------- |
| Checkout do código  | Obtém o código do repositório Azure Repos       |
| Java Tool Installer | Configura o Java 17                             |
| Gradle Build        | Executa: `./gradlew build`                      |
| Azure WebApp Deploy | Faz deploy do arquivo `.jar` para o App Service |

3.4 Adicionando Variáveis no Pipeline
-Na pipeline, clique em Variables e adicione:
| Nome                       | Valor                                                                               |
| -------------------------- | ----------------------------------------------------------------------------------- |
| SPRING_DATASOURCE_USERNAME | admsql                                                                              |
| SPRING_DATASOURCE_PASSWORD | Fiap@2tdsvms                                                                        |
| SPRING_DATASOURCE_URL      | jdbc:sqlserver://sqlserver-rm557462.database.windows.net:1433;database=ondetamotodb;encrypt=true;trustServerCertificate=false;|

---

## 🔬 Parte 4: Verificação e Testes

### 4.1 Verificando as Tabelas no Banco de Dados

Após a conclusão do deploy pelo GitHub Actions, o Flyway deverá ter executado as migrations e criado as tabelas.

1.  No Portal Azure, vá para o seu banco de dados `ondetamotodb`.
2.  No menu lateral, selecione **Editor de Consultas (visualização)**.
3.  Faça o login com a **Autenticação do SQL Server**:
    * **Login**: `admsql`
    * **Senha**: `Fiap@2tdsvms`
4.  Execute as seguintes consultas para verificar se as tabelas foram criadas e se contêm dados:

    ```sql
    select * from estabelecimento;
    select * from setores;
    select * from moto;
    select * from usuario;
    ```

### 4.2 Testando a API com Requisições

## 🔗 Rotas Pricipais pra Teste (Swagger e Thymeleaf)

A API do projeto podia ser acessada via Swagger na rota:

[https://ondetamoto-rm557462.azurewebsites.net/swagger-ui/index.html](https://ondetamoto-rm557462.azurewebsites.net/swagger-ui/index.html)

Tambem pode acessar as páginas criadas com o thymeleaf (Recomendado):

[https://ondetamoto-rm557462.azurewebsites.net/login](https://ondetamoto-rm557462.azurewebsites.net/login)

> **Importante:**
> Crie um **Estabelecimento** antes de criar um **Setor** e crie um **Setor** antes de adicionar uma **Moto**. O ID gerado em um passo é usado no próximo.

## Rotas recomendadas para o Teste:
#### Exemplo 1: (Registrar Usuário)

```bash
{
    "email": "henriquechaco@gmail.com",
    "senha": "SenhaForte123",
    "role": "ADMIN"
}
```
#### Exemplo 1.5: (Logar Usuário)

```bash
{
    "email": "henriquechaco@gmail.com",
    "senha": "SenhaForte123"
}
```

#### Exemplo 2: (Criar Estabelecimento)

```bash
{
    "endereco": "Avenida Ale de Vasconcelos 362",
    "usuarioId": 1
}
```

#### Exemplo 3: (Criar Setor)

```bash
{
    "nome": "Ala de Reparos Rápidos",
    "tipo": "MANUTENCAO",
    "tamanho": "Grande",
    "idEstabelecimento": 1
}
```

#### Exemplo 4: (Adicionar Moto)


```bash
{
    "marca": "Honda",
    "placa": "XYZ1234",
    "tag": "MT-01",
    "idSetores": 1
}
```

---

## 💡 Considerações Finais e Troubleshooting

* **Flyway**: Verifique se os seus scripts de migração do Flyway (`V1__create_table.sql`, etc.) estão corretos na pasta `src/main/resources/db/migration` do seu projeto. Erros aqui são uma causa comum de falha na inicialização da aplicação.
* **Dependências**: Confirme se o seu arquivo `build.gradle` ou `pom.xml` contém todas as dependências necessárias (Spring Web, Spring Data JPA, SQL Server Driver, Flyway, etc.).
* **Logs**: Se a aplicação falhar ao iniciar, verifique os logs. Vá para o App Service no Portal Azure > **Ferramentas de Desenvolvimento** > **Fluxo de Log** para ver os logs em tempo real.

---

## 🧑‍💻 Integrantes do Grupo

- **Guilherme Romanholi Santos - RM557462**
- **Murilo Capristo - RM556794**
- **Nicolas Guinante Cavalcanti - RM557844**
