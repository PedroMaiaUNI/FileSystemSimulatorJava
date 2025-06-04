
# Simulador de Sistema de Arquivos com Journaling

## 📜 Metodologia

LINK DO REPOSITÓRIO: https://github.com/PedroMaiaUNI/FileSystemSimulatorJava

O simulador foi desenvolvido utilizando a linguagem de programação **Java**. Seu funcionamento se dá por meio da execução de métodos que simulam operações típicas de sistemas de arquivos, como criação, deleção, cópia, escrita, leitura, entre outros.

As operações são refletidas tanto na estrutura interna do simulador quanto em um **journal**, que registra todas as modificações, permitindo a recuperação dos dados em caso de falha.

O programa executa as funcionalidades e exibe os resultados no console quando necessário.

---

## 🗂️ Parte 1: Introdução ao Sistema de Arquivos com Journaling

### 🔹 O que é um Sistema de Arquivos?

Um sistema de arquivos é um componente essencial dos sistemas operacionais que gerencia como os dados são armazenados, organizados e recuperados em dispositivos de armazenamento, como HDs, SSDs ou pendrives.

Ele permite a criação de arquivos e diretórios, bem como a manipulação desses dados de forma eficiente e organizada.

### 🔸 O que é Journaling?

O **Journaling** é uma técnica utilizada por sistemas de arquivos para garantir a integridade dos dados em caso de falhas, como quedas de energia ou travamentos.

Antes de executar uma operação, o sistema registra no journal (um log) a intenção da ação. Isso permite que, caso o processo seja interrompido, o sistema possa recuperar ou reverter operações inacabadas, mantendo o sistema de arquivos em um estado consistente.

### 🔸 Tipos de Journaling:

- **Write-Ahead Logging (WAL)**: Registra as operações no log antes de aplicá-las no sistema de arquivos. Se ocorrer uma falha, é possível refazer ou desfazer as operações com base no journal.
- **Log-Structured File System (LFS)**: Organiza todos os dados e metadados como uma sequência contínua no log, tornando o log o próprio sistema de arquivos.

Neste projeto, foi implementado o modelo **Write-Ahead Logging**, no qual cada operação é registrada antes de ser salva no arquivo de persistência.

---

## 🏗️ Parte 2: Arquitetura do Simulador

### 🔹 Estrutura de Dados

O simulador utiliza as seguintes estruturas:

- **Classe `FileSystemSimulator`**: Gerencia o sistema de arquivos, mantendo a estrutura de diretórios e arquivos, além de fornecer métodos para realizar operações como criar, deletar, renomear, copiar, colar e escrever arquivos.

- **Classe `Entity`** *(abstrata)*: Base para arquivos e diretórios, contendo atributos como nome, data de criação e métodos comuns.

- **Classe `FileType`**: Representa um arquivo, armazenando seu conteúdo como uma string. (Detalhe, essa seria a classe "File", mas esse nome foi escolhido durante o desenvolvimento para não gerar confusão com a classe File nativa do Java.)

- **Classe `Directory`**: Representa um diretório, contendo listas de arquivos e subdiretórios.

### 🔸 Implementação do Journaling

O journaling é implementado na classe `Journal`, que registra no arquivo `journal.log` cada operação realizada no simulador.

As operações registradas incluem:

- Criação e deleção de arquivos e diretórios
- Renomeações
- Escritas em arquivos
- Operações de cópia, corte e colagem
- Alteração de diretórios
- Duplicações

Também há um comando especial, **`CHECKPOINT`**, que marca que o programa foi salvo com sucesso, permitindo que, na próxima inicialização, apenas as operações após o último checkpoint sejam recuperadas.

---

## 💻 Parte 3: Implementação em Java

### 🔹 Principais Classes

#### ✅ `FileSystemSimulator`
- Gerencia a árvore de arquivos e diretórios.
- Implementa operações de sistema de arquivos, como:
  - Criar arquivo/diretório
  - Deletar
  - Renomear
  - Copiar e colar 
  - Navegar entre diretórios
- Funcionalidades extra:
  - Cortar e duplicar
  - Ler/escrever arquivos
- Interage diretamente com a classe `Journal` para registrar cada operação.

#### ✅ `FileType` e `Directory`
- `Entity`: Representa qualquer elemento presente no disco. Classe-pai de FileType e Directory.
- `FileType`: Representa um arquivo com nome, extensão e conteúdo.
- `Directory`: Representa um diretório contendo arquivos e outros diretórios.

#### ✅ `Journal`
- Responsável pelo gerenciamento do log de operações.
- Permite:
  - Registrar operações no arquivo `journal.log`.
  - Recuperar o sistema de arquivos a partir dos registros (após falhas).
  - Marcar checkpoints para evitar reaplicação de operações anteriores.

---

## 🚀 Parte 4: Instalação e Funcionamento

### ✅ Requisitos:
- **Java JDK 17** ou superior
- Editor de código (ex.: IntelliJ, VSCode, Eclipse) ou terminal com compilador Java

### ✅ Passos para executar:

1. Clone o repositório:

```bash
git clone https://github.com/seu-usuario/seu-repositorio.git
cd seu-repositorio
```

2. Compile o projeto:

```bash
javac *.java
```

3. Execute o simulador:

```bash
java Main
```


### ✅ Funcionamento básico:

- O simulador inicia carregando o estado do sistema a partir do arquivo `journal.log` (se existir).
- As operações podem ser executadas por meio de menus no terminal ou chamadas de métodos, dependendo da implementação.
- Ao fechar corretamente o programa, é criado um **checkpoint** no log, garantindo que operações anteriores não precisem ser reaplicadas na próxima execução.

### ✅ Estrutura de arquivos gerados:

- **`journal.log`** → Arquivo de log com todas as operações.
- *(Outros arquivos, caso você implemente persistência de arquivos e diretórios fora do journal.)*

---
