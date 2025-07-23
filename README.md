# **Casa Moreno - Backend API**

O backend da plataforma de curadoria de produtos Casa Moreno, construído com Java e Spring Boot.

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-brightgreen)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-%E2%9C%94-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring%20Security-%E2%9C%94-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14.5-blue)
![Flyway](https://img.shields.io/badge/Flyway-%E2%9C%94-orange)
![OpenFeign](https://img.shields.io/badge/OpenFeign-%E2%9C%94-green)
![Google Gemini](https://img.shields.io/badge/Google%20Gemini-%E2%9C%94-blue)
![Amazon AWS](https://img.shields.io/badge/Amazon%20AWS-%23232F3E.svg?logo=amazon-aws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-%E2%9C%94-blue)
![Maven](https://img.shields.io/badge/Maven-%E2%9C%94-red)
![Springdoc OpenAPI](https://img.shields.io/badge/Springdoc%20OpenAPI-2.5.0-blue)
![Lombok](https://img.shields.io/badge/Lombok-%E2%9C%94-purple)

-----

## ❯ Sobre o Projeto

O **Casa Moreno - Backend** é o sistema de servidor para a aplicação **[Casa Moreno](https://www.casa-moreno.com)**, uma plataforma de curadoria de produtos de tecnologia. O sistema foi projetado para ser uma solução robusta, escalável e segura, servindo como a espinha dorsal para todas as operações do site.

O modelo de negócio não é de um e-commerce tradicional. Em vez disso, o backend gerencia um catálogo de produtos obtidos de grandes varejistas, atuando como uma vitrine inteligente. Ele centraliza as melhores ofertas, e ao escolher um produto, o usuário é redirecionado para o site parceiro para concluir a compra, garantindo transparência e segurança.

### Principais Funcionalidades

- **API RESTful Completa:** Endpoints para gerenciamento de produtos, usuários, autenticação e integrações.
- **Sistema de Autenticação Duplo:** Suporte para login tradicional com **JWT** e login social via **Google OAuth2**, oferecendo flexibilidade e segurança.
- **Gerenciamento de Usuários:** Operações CRUD para usuários, incluindo gerenciamento de perfis, recuperação de senha via e-mail e upload de fotos de perfil para o **AWS S3**.
- **Catálogo de Produtos Inteligente:** Cadastro de produtos que pode ser populado manualmente ou de forma automática através da integração com um serviço de *scraping* via **OpenFeign**.
- **Integração com IA (Google Gemini):**
    - Um chatbot assistente para responder dúvidas dos usuários com base em um contexto pré-definido da loja.
    - Uma ferramenta para formatar e otimizar descrições de produtos, melhorando a experiência do usuário.
- **Notificações por E-mail:** Sistema de e-mails transacionais assíncronos para eventos como cadastro, redefinição de senha e boas-vindas.
- **CI/CD Automatizado:** Pipeline de integração e implantação contínua com **GitHub Actions** para automação de build, testes, containerização com **Docker** e deploy na **AWS EC2**.

-----

## ❯ Visão Geral da Arquitetura

O sistema foi desenvolvido seguindo as melhores práticas de arquitetura de software, com uma clara separação de responsabilidades entre as camadas.

1.  **API RESTful (Spring Web MVC):** O núcleo da aplicação, que expõe os endpoints para o frontend e serviços externos.
2.  **Camada de Segurança (Spring Security):** Responsável por proteger os endpoints. Utiliza uma abordagem dupla:
    - **Autenticação baseada em JWT:** Para usuários que se cadastram com e-mail e senha. O `LoginController` valida as credenciais e o `TokenService` gera um token JWT com as informações do usuário e suas permissões (`ROLE_USER` ou `ROLE_ADMIN`).
    - **Google OAuth2:** Para login social. O `CustomOAuth2AuthenticationSuccessHandler` gerencia o fluxo, criando um usuário no banco de dados, se necessário, e gerando um token JWT para a sessão do cliente.
3.  **Lógica de Negócio (Services):**
    - `ProductService`: Orquestra as regras de negócio para produtos, incluindo a chamada ao `MercadoLivreScraperClient`.
    - `UserService`: Gerencia a lógica de usuários, senhas e perfis.
    - `EmailService`: Lida com o envio assíncrono de e-mails usando `JavaMailSender` e templates `Thymeleaf`.
    - `GeminiService`: Encapsula a lógica de interação com a API do Google Gemini.
4.  **Acesso a Dados (Spring Data JPA):**
    - A camada de persistência utiliza o padrão de repositórios do Spring Data JPA para interagir com o banco de dados.
    - As entidades (`Product`, `User`) mapeiam as tabelas do banco de dados.
5.  **Banco de Dados (PostgreSQL & Flyway):**
    - Um banco de dados **PostgreSQL**, hospedado no **AWS RDS**, é utilizado para persistir os dados.
    - O **Flyway** é responsável por gerenciar as migrações do banco de dados, garantindo que o schema esteja sempre consistente entre diferentes ambientes.
6.  **Integrações Externas:**
    - **Mercado Livre Scraper (OpenFeign):** A comunicação com o microserviço de scraping é feita de forma declarativa e resiliente com o OpenFeign e o **Resilience4j** (Circuit Breaker) para prevenir falhas em cascata.
    - **AWS S3 (AWS SDK for Java):** O `S3StorageAdapter` implementa a lógica para fazer upload e deletar imagens de perfil dos usuários, armazenando-as de forma segura e escalável.
    - **Google Gemini (Vertex AI SDK):** Integração direta com a IA do Google para processamento de linguagem natural.
7.  **DevOps (Docker & GitHub Actions):**
    - A aplicação é containerizada com **Docker**.
    - O arquivo `main.yml` define um pipeline no **GitHub Actions** que automatiza todo o processo de deploy: build do Maven -\> build da imagem Docker -\> push para o Docker Hub -\> deploy na AWS EC2.

-----

## ❯ Tecnologias Utilizadas

Este projeto foi construído com um ecossistema moderno e robusto de tecnologias:

- **Linguagem & Framework Core:**

    - **Java 21:** Versão mais recente da linguagem Java (LTS).
    - **Spring Boot 3.3+:** Framework principal para a construção da aplicação.
    - **Spring Web:** Para a criação de APIs RESTful.
    - **Spring Data JPA:** Para a camada de persistência de dados.
    - **Hibernate:** Implementação do JPA utilizada.

- **Segurança:**

    - **Spring Security:** Para autenticação e autorização.
    - **JSON Web Tokens (JWT):** Para a autenticação stateless.
    - **OAuth 2.0:** Para integração com o login do Google.

- **Banco de Dados:**

    - **PostgreSQL:** Banco de dados relacional.
    - **Flyway:** Para o versionamento e migração do schema do banco de dados.

- **Cloud & Infraestrutura (AWS):**

    - **Amazon EC2:** Para hospedar a aplicação containerizada.
    - **Amazon S3:** Para armazenamento de objetos (fotos de perfil dos usuários).
    - **Amazon RDS:** Serviço gerenciado para o banco de dados PostgreSQL.

- **DevOps & Ferramentas:**

    - **Docker:** Para a containerização da aplicação.
    - **GitHub Actions:** Para a automação do pipeline de CI/CD.
    - **Maven:** Para gerenciamento de dependências e build do projeto.

- **Integrações e Outros:**

    - **OpenFeign:** Para a criação de clientes REST declarativos.
    - **Resilience4j:** Para a implementação do padrão Circuit Breaker.
    - **Spring Mail + Thymeleaf:** Para o envio de e-mails com templates HTML.
    - **Google Cloud Vertex AI (Gemini):** Para funcionalidades de inteligência artificial.
    - **SpringDoc (Swagger):** Para a documentação interativa da API.

-----

## ❯ Documentação da API

A API está documentada utilizando **SpringDoc**, que gera uma interface **Swagger UI**. Você pode acessar e interagir com os endpoints em:

```
/casa-moreno-docs
```

A interface permite visualizar todos os endpoints disponíveis, seus parâmetros, respostas esperadas e até mesmo testá-los diretamente, desde que você tenha um token de autenticação válido para as rotas protegidas.