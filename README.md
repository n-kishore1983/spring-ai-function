# Spring AI Function Application - Architecture Design

## System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT APPLICATION                                   │
└────────────────────────────────────┬─────────────────────────────────────────────┘
                                     │
                                     │ HTTP POST /ask
                                     │ { "question": "..." }
                                     ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                          SPRING BOOT APPLICATION                                  │
│ ┌──────────────────────────────────────────────────────────────────────────────┐ │
│ │                         CONTROLLER LAYER                                     │ │
│ │  ┌────────────────────────────────────────────────────────────────────┐     │ │
│ │  │  QuestionController                                                │     │ │
│ │  │  - @RestController                                                 │     │ │
│ │  │  - POST /ask                                                       │     │ │
│ │  │  - Receives Question                                               │     │ │
│ │  │  - Returns Answer                                                  │     │ │
│ │  └─────────────────────────────┬──────────────────────────────────────┘     │ │
│ └────────────────────────────────┼──────────────────────────────────────────────┘ │
│                                  │                                                │
│ ┌────────────────────────────────▼──────────────────────────────────────────────┐ │
│ │                         SERVICE LAYER                                        │ │
│ │  ┌────────────────────────────────────────────────────────────────────┐     │ │
│ │  │  OpenAIServiceImpl                                                 │     │ │
│ │  │  - @Service                                                        │     │ │
│ │  │  - OpenAiApi injection                                             │     │ │
│ │  │  - Creates OpenAiChatModel                                         │     │ │
│ │  │  - Creates ChatClient                                              │     │ │
│ │  │  - BeanOutputConverter<InflationResponse>                          │     │ │
│ │  └─────────────────────┬──────────────────────┬───────────────────────┘     │ │
│ └────────────────────────┼──────────────────────┼─────────────────────────────┘ │
│                          │                      │                               │
│                          │                      │ Registers Tools               │
│                          │                      │                               │
│                          │                      ▼                               │
│ ┌────────────────────────┼──────────────────────────────────────────────────────┐ │
│ │                        │            TOOLS LAYER                               │ │
│ │                        │      ┌────────────────────────────────────────┐     │ │
│ │                        │      │  NinjaTools (@Service)                 │     │ │
│ │                        │      │  - RestClient (api-ninjas.com)         │     │ │
│ │                        │      │                                        │     │ │
│ │                        │      │  @Tool Methods:                        │     │ │
│ │                        │      │  1. get_inflation_data()               │     │ │
│ │                        │      │     - Input: InflationRequest          │     │ │
│ │                        │      │     - Output: InflationResponse        │     │ │
│ │                        │      │                                        │     │ │
│ │                        │      │  2. convert_currency()                 │     │ │
│ │                        │      │     - Input: CurrencyConversionRequest │     │ │
│ │                        │      │     - Output: CurrencyConversionResp.  │     │ │
│ │                        │      └──────────────┬─────────────────────────┘     │ │
│ └────────────────────────┼───────────────────────┼─────────────────────────────┘ │
│                          │                       │                               │
│                          │                       │ HTTP Requests                 │
│                          │                       │                               │
│ ┌────────────────────────▼───────────────────────▼─────────────────────────────┐ │
│ │                    EXCEPTION HANDLING LAYER                                  │ │
│ │  ┌────────────────────────────────────────────────────────────────────┐     │ │
│ │  │  GlobalExceptionHandler                                            │     │ │
│ │  │  - @ControllerAdvice                                               │     │ │
│ │  │  - @ExceptionHandler(AIException.class)                            │     │ │
│ │  │  - Returns ErrorResponse (status, message, timestamp)              │     │ │
│ │  └────────────────────────────────────────────────────────────────────┘     │ │
│ └──────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
│ ┌──────────────────────────────────────────────────────────────────────────────┐ │
│ │                           MODEL LAYER                                        │ │
│ │  Request Models:          Response Models:         Exception:                │ │
│ │  - Question               - Answer                 - AIException             │ │
│ │  - InflationRequest       - InflationResponse      - ErrorResponse           │ │
│ │  - CurrencyConversionReq  - CurrencyConversionResp                           │ │
│ └──────────────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────┬──────────────────────┬──────────────────────────────┘
                             │                      │
                             │                      │
                ┌────────────▼──────────┐  ┌────────▼────────────────┐
                │   OpenAI API          │  │   API Ninjas            │
                │   - GPT-4o Model      │  │   - /inflation          │
                │   - Function Calling  │  │   - /exchangerate       │
                │   - Chat Completion   │  │                         │
                └───────────────────────┘  └─────────────────────────┘


## Data Flow Sequence

┌──────┐         ┌────────────┐         ┌─────────────┐         ┌──────────┐
│Client│         │ Controller │         │   Service   │         │  Tools   │
└──┬───┘         └─────┬──────┘         └──────┬──────┘         └────┬─────┘
   │                   │                       │                      │
   │ POST /ask         │                       │                      │
   │ {question}        │                       │                      │
   ├──────────────────►│                       │                      │
   │                   │                       │                      │
   │                   │ getAnswer(question)   │                      │
   │                   ├──────────────────────►│                      │
   │                   │                       │                      │
   │                   │                       │ Build ChatClient     │
   │                   │                       │ with OpenAI API      │
   │                   │                       ├──────┐               │
   │                   │                       │      │               │
   │                   │                       │◄─────┘               │
   │                   │                       │                      │
   │                   │                       │ Register Tools       │
   │                   │                       ├─────────────────────►│
   │                   │                       │                      │
   │                   │                       │ Send prompt to       │
   │                   │                       │ OpenAI with tools    │
   │                   │                       ├────────┐             │
   │                   │                       │        │             │
   │                   │                       │◄───────┘             │
   │                   │                       │                      │
   │                   │                       │ ◄────────────────────┤
   │                   │                       │  OpenAI calls tool   │
   │                   │                       │  (if needed)         │
   │                   │                       │                      │
   │                   │                       │ Call Ninja API       │
   │                   │                       │ ────────────────────►│
   │                   │                       │                      ├──┐
   │                   │                       │                      │  │ HTTP GET
   │                   │                       │                      │  │ to API Ninjas
   │                   │                       │                      │◄─┘
   │                   │                       │                      │
   │                   │                       │◄─────────────────────┤
   │                   │                       │  Return data         │
   │                   │                       │                      │
   │                   │                       │ Convert response     │
   │                   │                       │ with BeanConverter   │
   │                   │                       ├──────┐               │
   │                   │                       │      │               │
   │                   │                       │◄─────┘               │
   │                   │                       │                      │
   │                   │ Return Answer         │                      │
   │                   │◄──────────────────────┤                      │
   │                   │                       │                      │
   │ 200 OK            │                       │                      │
   │ {answer}          │                       │                      │
   │◄──────────────────┤                       │                      │
   │                   │                       │                      │
```

## Component Details

### 1. **Controller Layer**
- **QuestionController**: REST endpoint that accepts user questions
  - Endpoint: `POST /ask`
  - Input: `Question` record with question text
  - Output: `Answer` record with InflationResponse

### 2. **Service Layer**
- **OpenAIServiceImpl**: Core business logic
  - Initializes OpenAI Chat Model
  - Creates ChatClient for AI interactions
  - Registers function tools
  - Uses BeanOutputConverter to transform AI responses to structured data
  - Handles null/empty responses with AIException

### 3. **Tools Layer**
- **NinjaTools**: External API integration with function calling
  - **get_inflation_data**: Retrieves inflation statistics
    - Country code (2-letter)
    - Inflation type (HICP, CPI)
  - **convert_currency**: Currency exchange rates
    - From currency
    - To currency
  - Uses RestClient for HTTP calls to api-ninjas.com

### 4. **Exception Handling**
- **GlobalExceptionHandler**: Centralized error handling
  - Catches AIException
  - Returns structured ErrorResponse
  - Logs errors for debugging

### 5. **Model Layer**
- **Request Models**: Question, InflationRequest, CurrencyConversionRequest
- **Response Models**: Answer, InflationResponse, CurrencyConversionResponse
- **Exceptions**: AIException, ErrorResponse

## Technology Stack

- **Framework**: Spring Boot 3.x
- **AI Integration**: Spring AI with OpenAI
- **LLM**: GPT-4o
- **External API**: API Ninjas (inflation & currency data)
- **Build Tool**: Maven
- **Java Version**: 21
- **Key Libraries**:
  - Spring Web
  - Spring AI OpenAI
  - Lombok
  - Jackson (JSON processing)

## Key Features

1. **AI-Powered Question Answering**: Natural language processing via OpenAI
2. **Function Calling**: AI can invoke tools to fetch real-time data
3. **Structured Output**: BeanOutputConverter ensures type-safe responses
4. **Error Handling**: Comprehensive exception management
5. **External API Integration**: Real-time inflation and currency data
6. **RESTful API**: Simple HTTP interface for client applications

## Configuration

- **OpenAI API Key**: Environment variable `OPENAI_API_KEY`
- **Ninjas API Key**: Environment variable `NINJAS_API_KEY`
- **Model Settings**: GPT-4o, temperature 0.7, max tokens 1000

## Example Use Cases

1. **Inflation Query**: "What's the latest inflation rate for Canada?"
   - AI calls `get_inflation_data` tool
   - Retrieves data from API Ninjas
   - Returns structured InflationResponse

2. **Currency Conversion**: "Convert USD to EUR"
   - AI calls `convert_currency` tool
   - Fetches exchange rate
   - Returns CurrencyConversionResponse
