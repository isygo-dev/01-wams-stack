# Open AI Service

This project provides a Spring Boot-based REST API for interacting with AI models, specifically integrating with
Google's Gemini API and the Ollama framework for local model inference. It allows users to generate content using either
the Gemini API or various Ollama-supported models.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Supported Ollama Models](#supported-ollama-models)
- [Testing](#testing)
- [Logging](#logging)
- [Contributing](#contributing)
- [License](#license)

## Features

- Integration with Google Gemini API for content generation.
- Support for local inference using Ollama with various open-source models.
- RESTful API endpoints for generating AI responses.
- Configurable parameters like temperature and max tokens.
- Comprehensive error handling for API calls.
- Integration tests using Testcontainers for Ollama and MockMvc for endpoint testing.

## Prerequisites

- Java 17 or higher
- Maven 3.8.0+
- Docker (for running Ollama and Testcontainers)
- Google Gemini API key (optional, for Gemini integration)
- Ollama installed locally or running via Docker (for Ollama integration)

## Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-repo/gemini-ai-service.git
   cd gemini-ai-service
   ```

2. **Install dependencies**:
   ```bash
   mvn clean install
   ```

3. **Set up Ollama (optional)**:
   If using Ollama, ensure it is running locally or via Docker:
   ```bash
   docker run -d -p 11434:11434 --name ollama ollama/ollama:latest
   ```

4. **Configure environment variables**:
   Create or update `application.yml` with your Gemini API key and Ollama settings (
   see [Configuration](#configuration)).

5. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

## Configuration

The application uses `application.yml` for configuration. Key settings include:

```yaml
server:
  port: 8081

#logging:
#  level:
#    org.springframework: DEBUG
#    org.springframework.data: DEBUG
#    org.hibernate: DEBUG
#    eu.isygoit: DEBUG
google:
  gemini:
    api:
      key: ${GEMINI_API_KEY:your-api-key}
      url: https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
      timeout: 30000
      max-retries: 3
ollama:
  api:
    url: http://localhost:11434
  model: qwen2.5:1.5b
```

- Replace `${GEMINI_API_KEY}` with your actual Gemini API key.
- Update `ollama.api.url` if Ollama is running on a different host/port.
- Specify the desired Ollama model (e.g., `qwen2.5:1.5b`).

## Usage

The application exposes REST endpoints for interacting with Gemini and Ollama models.

### Example API Calls

#### Gemini API

```bash
curl "http://localhost:8081/api/v1/chat/ai/gemini/generate?message=Translate%20to%20Arabic:%20Hello,%20how%20can%20I%20help%20you"
```

#### Ollama API

```bash
curl "http://localhost:8081/api/v1/chat/ai/ollama/generate?message=Translate%20to%20French:%20Hello,%20how%20can%20I%20help%20you?&temperature=0.8&maxTokens=100"
```

### Response Format

Both endpoints return a JSON response in the format:

```json
{
  "success": true,
  "generatedText": "Translated text or generated content",
  "errorMessage": null
}
```

On error:

```json
{
  "success": false,
  "generatedText": null,
  "errorMessage": "Error message"
}
```

## API Endpoints

- **Gemini Generate Content**:
    - `GET /api/v1/chat/ai/gemini/generate`
    - Parameters:
        - `message` (required): The input prompt.
        - `temperature` (optional): Controls randomness (0.0 to 1.0).
        - `maxTokens` (optional): Maximum number of output tokens.
    - Returns: `GeminiResponse` with generated text or error.

- **Ollama Generate Content**:
    - `GET /api/v1/chat/ai/ollama/generate`
    - Parameters:
        - `message` (required): The input prompt.
        - `temperature` (optional): Controls randomness (0.0 to 1.0).
        - `maxTokens` (optional): Maximum number of output tokens.
    - Returns: `GeminiResponse` with generated text or error.

# Comparison of Local LLM Deployment Tools

This part provides a comparative overview of tools for running large language models (LLMs) locally, including Ollama
and its alternatives. Each tool is evaluated based on key criteria to help users choose the best option for their needs.

## Overview of Ollama

Ollama is an open-source tool designed to simplify the deployment and operation of LLMs locally. It supports macOS,
Linux, and Windows (in preview), offering a simple CLI, API support, model customization, and offline operation with
over 150 models.

## Comparative Table

| **Tool**       | **Open Source** | **Platforms**                   | **Key Features**                                                             | **Ease of Use**                   | **Performance**                      | **Best For**                                                           |
|----------------|-----------------|---------------------------------|------------------------------------------------------------------------------|-----------------------------------|--------------------------------------|------------------------------------------------------------------------|
| **Ollama**     | Yes             | macOS, Linux, Windows (preview) | Simple CLI, API support, model customization, offline operation, 150+ models | High (CLI-focused, simple setup)  | Good (GPU support, CPU fallback)     | Beginners, privacy-focused users, quick local LLM deployment           |
| **LM Studio**  | No (Free)       | Windows, macOS, Linux           | GUI-based, model discovery, offline mode, supports Hugging Face models       | Very High (visual interface)      | Good (optimized for local use)       | Non-technical users, GUI enthusiasts, cross-platform deployment        |
| **LocalAI**    | Yes             | Windows, macOS, Linux           | OpenAI-compatible API, no GPU required, supports multiple model formats      | Moderate (CLI, optional WebUI)    | Moderate (CPU-focused, GPU optional) | Budget-conscious developers, flexible model support                    |
| **Nut Studio** | No (Free)       | Windows, macOS, Linux           | GUI-based, no-code setup, 50+ models, offline mode, AI agent creation        | Very High (no CLI needed)         | Good (CPU/GPU support)               | Beginners, non-coders, Windows users seeking simplicity                |
| **vLLM**       | Yes             | Linux (primarily)               | High-throughput, memory-efficient, in-flight batching, multi-node support    | Moderate (requires setup skills)  | Excellent (GPU-optimized)            | Production environments, high-performance needs                        |
| **KoboldCPP**  | Yes             | Windows, macOS, Linux           | Supports GGUF/GGML, image generation, OpenAI-style API, CUDA acceleration    | Moderate (CLI with UI options)    | Good (fast with GPU)                 | Developers experimenting with model formats, image generation          |
| **Jan.ai**     | Yes             | Windows, macOS, Linux           | GUI and CLI, privacy-focused, offline mode, community-driven                 | High (GUI simplifies interaction) | Good (optimized for local use)       | Open-source enthusiasts, privacy-focused developers                    |
| **Llama.cpp**  | Yes             | Windows, macOS, Linux           | Lightweight, efficient, supports LLaMA-based models, CPU/GPU optimization    | Moderate (CLI-focused)            | Excellent (highly optimized)         | Advanced users, performance-focused developers, minimal resource usage |

## Tool Descriptions

- **Ollama**: Streamlined for local LLM deployment with a focus on privacy and ease. Ideal for users who want a quick
  setup and broad model support.
- **LM Studio**: A GUI-based tool for non-technical users, offering model discovery and offline capabilities. Best for
  those preferring a visual interface.
- **LocalAI**: Open-source with OpenAI-compatible APIs, runs on modest hardware. Suitable for developers needing
  flexibility without high-end GPUs.
- **Nut Studio**: GUI-driven, no-code platform for beginners. Supports 50+ models and AI agent creation, perfect for
  non-coders.
- **vLLM**: Optimized for high-throughput inference in production. Best for advanced users with GPU resources and
  technical expertise.
- **KoboldCPP**: Supports diverse model formats and image generation. Good for developers experimenting with GGUF/GGML
  models.
- **Jan.ai**: Community-driven, privacy-focused tool with GUI and CLI options. Great for open-source enthusiasts.
- **Llama.cpp**: Highly optimized for LLaMA-based models, lightweight, and efficient. Ideal for advanced users
  prioritizing performance.

## Choosing the Right Tool

- **For Beginners**: Nut Studio or LM Studio for their intuitive GUIs.
- **For Privacy**: Ollama or Jan.ai for offline, secure operation.
- **For Performance**: vLLM or Llama.cpp for GPU-optimized, high-throughput needs.
- **For Flexibility**: LocalAI or KoboldCPP for diverse model support and hardware compatibility.

For more details, visit the official documentation of each tool or explore community discussions on platforms like
Reddit.

## Supported Ollama Models

Ollama supports a variety of models that can be pulled and used locally. To use a specific model, update the
`ollama.model` property in `application.yml`. The following table lists officially supported models maintained by
Ollama, along with details on size, pertinence, precision, licensing, and scope.

| Model Name            | Size (Parameters) | Pertinence | Precision | Free | Scope                            | Notes                                                              |
|-----------------------|-------------------|------------|-----------|------|----------------------------------|--------------------------------------------------------------------|
| **llama2**            | 7B                | High       | FP16      | Yes  | General-purpose, dialogue        | Default Llama 2 model, optimized for dialogue and general tasks.   |[](https://ollama.com/library)
| **llama2:13b**        | 13B               | High       | FP16      | Yes  | General-purpose, dialogue        | Larger model for improved performance. Requires 16GB RAM.          |[](https://github.com/ollama/ollama)
| **llama2:70b**        | 70B               | High       | FP16      | Yes  | General-purpose, dialogue        | High-performance model, requires 32GB RAM.                         |[](https://github.com/ollama/ollama)
| **llama2-uncensored** | 7B                | Moderate   | FP16      | Yes  | General-purpose, less restricted | Less restrictive version of Llama 2 for more open-ended tasks.     |[](https://ollama.com/library)
| **codellama**         | 7B                | High       | FP16      | Yes  | Code generation                  | Optimized for code-related tasks.                                  |[](https://ollama.com/library)
| **codellama:13b**     | 13B               | High       | FP16      | Yes  | Code generation                  | Larger code-focused model, requires 16GB RAM.                      |[](https://ollama.com/library)
| **codellama:34b**     | 34B               | High       | FP16      | Yes  | Code generation                  | High-capacity code model, requires significant resources.          |[](https://ollama.com/library)
| **codellama:70b**     | 70B               | High       | FP16      | Yes  | Code generation                  | Largest code model, requires 32GB+ RAM.                            |[](https://ollama.com/library)
| **codellama:python**  | 7B                | High       | FP16      | Yes  | Python-specific code             | Specialized for Python programming tasks.                          |[](https://ollama.com/library)
| **mistral**           | 7B                | High       | FP16      | Yes  | General-purpose, dialogue        | Default Mistral model, outperforms Llama 2 13B on many benchmarks. |[](https://klu.ai/blog/open-source-llm-models)
| **mistral-openorca**  | 7B                | High       | FP16      | Yes  | Instruction-following            | Fine-tuned for instruction-following tasks.                        |[](https://ollama.com/library)
| **mixtral**           | 8x7B (MoE)        | High       | FP16      | Yes  | General-purpose, coding          | Mixture of Experts model, excels in coding and reasoning.          |[](https://ollama.com/library)
| **gemma**             | 2B                | Moderate   | FP16      | Yes  | General-purpose, lightweight     | Lightweight model for resource-constrained devices.                |[](https://ollama.com/library)
| **gemma:7b**          | 7B                | High       | FP16      | Yes  | General-purpose, lightweight     | Larger Gemma model, suitable for various tasks.                    |[](https://ollama.com/library)
| **phi**               | 2.7B              | Moderate   | FP16      | Yes  | General-purpose, efficient       | Microsoft’s small but powerful model.                              |[](https://ollama.com/library)
| **stablelm2**         | 1.6B              | Moderate   | FP16      | Yes  | General-purpose, lightweight     | Efficient model from Stability AI.                                 |[](https://ollama.com/library)
| **neural-chat**       | 7B                | High       | FP16      | Yes  | Conversational                   | Intel’s fine-tuned Mistral for conversational tasks.               |[](https://ollama.com/library)
| **dolphin-mixtral**   | 8x7B (MoE)        | High       | FP16      | Yes  | General-purpose, coding          | Uncensored Mixtral variant, excels in coding.                      |[](https://ollama.com/library)
| **starling-lm**       | 7B                | High       | FP16      | Yes  | Conversational, RLHF-tuned       | RLHF-tuned for improved dialogue performance.                      |[](https://ollama.com/library)
| **qwen2.5:1.5b**      | 1.5B              | High       | FP16      | Yes  | General-purpose, multilingual    | Default model in this project, supports 128K tokens, multilingual. |[](https://ollama.com/library?sort=newest)
| **deepseek-r1**       | 8B, 70B           | High       | FP16      | Yes  | Reasoning, general-purpose       | High-performance reasoning model, approaches Gemini 2.5 Pro.       |[](https://ollama.com/library)
| **phi-4**             | 14B               | High       | FP16      | Yes  | Reasoning, multilingual          | Advanced reasoning model from Microsoft, rivals larger models.     |[](https://ollama.com/library?sort=newest)
| **gemma3**            | 2B, 12B, 27B      | High       | FP16      | Yes  | Multimodal, reasoning            | Multimodal model with vision-language support, 128K context.       |[](https://deepinfra.com/)
| **mistral-small-3.1** | 24B               | High       | FP16      | Yes  | Multimodal, reasoning            | Enhanced vision and text capabilities, 128K context.               |[](https://deepinfra.com/)

**Notes**:

- **Size**: Indicates the number of parameters (e.g., 7B = 7 billion). Mixture of Experts (MoE) models like Mixtral have
  multiple 7B experts.
- **Pertinence**: High (widely used, strong performance), Moderate (niche or less optimized).
- **Precision**: FP16 is standard for most models; some support quantization (e.g., Q4, Q8) for lower resource usage.
- **Free**: All listed models are free to use under open-source licenses (e.g., Apache 2.0, MIT).
- **Scope**: General-purpose (dialogue, text completion), code generation, instruction-following, or multimodal (text
  and vision).
- **Hardware Requirements**: 7B models require ~8GB RAM, 13B ~16GB, 70B ~32GB. GPU recommended for optimal
  performance.[](https://github.com/ollama/ollama)

To pull a model:

```bash
docker exec ollama ollama pull <model-name>
```

## Testing

The project includes integration tests for both Gemini and Ollama endpoints.

### Running Tests

```bash
mvn test
```

### Test Details

- **GeminiIntegrationTest**: Tests the Gemini API endpoint using MockMvc.
- **OllamaIntegrationTest**: Uses Testcontainers to spin up an Ollama container and tests the Ollama API endpoint.

Ensure Docker is running for Testcontainers-based tests.

## Logging

Logging is configured in `application.yml`:

- Logs are written to `logs/application.log`.
- Log level for `eu.isygoit.openai` is set to `DEBUG` for detailed output.
- Root log level is `INFO`.

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.