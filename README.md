# Spring AI Function Application - Architecture Design

## System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT APPLICATION                                   │
└────────────────────────────────────────┬─────────────────────────────────────────┘
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
│ │  │  - Returns Answer (String response)                                │     │ │
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
│ │  │  - Returns natural language response                               │     │ │
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
│ │                        │      │                                        │     │ │
│ │                        │      │  3. stock_price()                      │     │ │
│ │                        │      │     - Input: StockPriceRequest         │     │ │
│ │                        │      │     - Output: StockPriceResponse       │     │ │
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
│ │  - StockPriceRequest      - StockPriceResponse                               │ │
│ └──────────────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────┬──────────────────────┬──────────────────────────────┘
                             │                      │
                             │                      │
                ┌────────────▼──────────┐  ┌────────▼────────────────┐
                │   OpenAI API          │  │   API Ninjas            │
                │   - GPT-4o Model      │  │   - /inflation          │
                │   - Function Calling  │  │   - /exchangerate       │
                │   - Chat Completion   │  │   - /stockprice         │
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
   │                   │                       │ AI generates         │
   │                   │                       │ natural language     │
   │                   │                       │ response             │
   │                   │                       ├──────┐               │
   │                   │                       │      │               │
   │                   │                       │◄─────┘               │
   │                   │                       │                      │
   │                   │ Return Answer         │                      │
   │                   │ (String)              │                      │
   │                   │◄──────────────────────┤                      │
   │                   │                       │                      │
   │ 200 OK            │                       │                      │
   │ {answer: "..."}   │                       │                      │
   │◄──────────────────┤                       │                      │
   │                   │                       │                      │
```

## Component Details

### 1. **Controller Layer**
- **QuestionController**: REST endpoint that accepts user questions
  - Endpoint: `POST /ask`
  - Input: `Question` record with question text
  - Output: `Answer` record with String response (natural language description)

### 2. **Service Layer**
- **OpenAIServiceImpl**: Core business logic
  - Initializes OpenAI Chat Model with injected OpenAiApi
  - Creates ChatClient for AI interactions
  - Registers function tools from NinjaTools
  - Returns natural language response describing the data
  - Handles null/empty responses with AIException

### 3. **Tools Layer**
- **NinjaTools**: External API integration with function calling
  - **get_inflation_data**: Retrieves inflation statistics
    - Input: Country code (2-letter), Inflation type (HICP, CPI)
    - Output: InflationResponse with rates and period
    - Endpoint: `/inflation`
  - **convert_currency**: Currency exchange rates
    - Input: From currency, To currency
    - Output: CurrencyConversionResponse with pair and rate
    - Endpoint: `/exchangerate`
  - **stock_price**: Get current stock price
    - Input: Stock ticker symbol (e.g., AAPL)
    - Output: StockPriceResponse with price, volume, exchange details
    - Endpoint: `/stockprice`
  - Uses RestClient for HTTP calls to api-ninjas.com
  - Comprehensive error handling with AIException

### 4. **Exception Handling**
- **GlobalExceptionHandler**: Centralized error handling
  - Catches AIException
  - Returns structured ErrorResponse
  - Logs errors for debugging

### 5. **Model Layer**
- **Request Models**: 
  - `Question` - User question text
  - `InflationRequest` - Country code and inflation type
  - `CurrencyConversionRequest` - From and to currency codes
  - `StockPriceRequest` - Stock ticker symbol
- **Response Models**: 
  - `Answer` - Natural language answer (String)
  - `InflationResponse` - Inflation data with rates, country, period
  - `CurrencyConversionResponse` - Currency pair and exchange rate
  - `StockPriceResponse` - Stock ticker, name, price, exchange, volume, currency
- **Exceptions**: 
  - `AIException` - Custom runtime exception for AI-related errors
  - `ErrorResponse` - Structured error response with status, message, timestamp

## Technology Stack

- **Framework**: Spring Boot 3.x
- **AI Integration**: Spring AI with OpenAI
- **LLM**: GPT-4o (temperature: 0.7, max tokens: 1000)
- **External API**: API Ninjas (inflation, currency, stock data)
- **Build Tool**: Maven
- **Java Version**: 21
- **Key Libraries**:
  - Spring Web
  - Spring AI OpenAI
  - Lombok
  - Jackson (JSON processing)
  - RestClient (HTTP communication)

## Key Features

1. **AI-Powered Question Answering**: Natural language processing via OpenAI GPT-4o
2. **Function Calling**: AI autonomously invokes tools to fetch real-time data
3. **Natural Language Output**: AI returns human-readable descriptions of data
4. **Error Handling**: Comprehensive exception management with GlobalExceptionHandler
5. **External API Integration**: Real-time inflation, currency, and stock market data
6. **RESTful API**: Simple HTTP interface for client applications
7. **Modular Tool Architecture**: Easy to extend with new data sources

## Configuration

- **OpenAI API Key**: Environment variable `OPENAI_API_KEY`
- **Ninjas API Key**: Environment variable `NINJAS_API_KEY`
- **Model Settings**: GPT-4o, temperature 0.7, max tokens 1000

## Example Use Cases

1. **Inflation Query**: "What's the latest inflation rate for Canada?"
   - AI calls `get_inflation_data` tool
   - Retrieves data from API Ninjas `/inflation` endpoint
   - Returns natural language description: "The latest inflation rate for Canada (CA) is 3.2% yearly for the period ending December 2025."

2. **Currency Conversion**: "Convert USD to EUR"
   - AI calls `convert_currency` tool
   - Fetches exchange rate from `/exchangerate` endpoint
   - Returns: "The current exchange rate for USD to EUR is 0.92."

3. **Stock Price Query**: "What's the current price of Apple stock?"
   - AI calls `stock_price` tool with ticker "AAPL"
   - Retrieves real-time data from `/stockprice` endpoint
   - Returns: "Apple Inc. (AAPL) is currently trading at $192.42 on NASDAQ with a volume of 44,594,000 shares."

## API Endpoints

### POST /ask
Request user questions and receive AI-powered answers.

**Request Body:**
```json
{
  "question": "What is the inflation rate in Germany?"
}
```

**Response:**
```json
{
  "answer": "The latest inflation rate for Germany (DE) is 2.8% yearly for HICP type inflation."
}
```

## Data Models

### InflationResponse
```json
{
  "country": "United States",
  "country_code": "US",
  "type": "CPI",
  "period": "2025-12",
  "monthly_rate_pct": 0.3,
  "yearly_rate_pct": 3.1
}
```

### CurrencyConversionResponse
```json
{
  "currency_pair": "USD_EUR",
  "exchange_rate": 0.92
}
```

### StockPriceResponse
```json
{
  "ticker": "AAPL",
  "name": "Apple Inc.",
  "price": 192.42,
  "exchange": "NASDAQ",
  "updated": 1706302801,
  "currency": "USD",
  "volume": 44594000
}
```

## Error Handling

When an error occurs, the API returns a structured error response:

```json
{
  "status": 500,
  "message": "Error while fetching inflation data: Connection timeout",
  "timestamp": 1706302801000
}
```

## Setup Instructions

1. **Clone the repository**
2. **Set environment variables:**
   - `OPENAI_API_KEY`: Your OpenAI API key
   - `NINJAS_API_KEY`: Your API Ninjas key
3. **Build the project:**
   ```bash
   ./mvnw clean install
   ```
4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## How It Works

1. Client sends a natural language question to `/ask` endpoint
2. `OpenAIServiceImpl` creates a ChatClient with GPT-4o model
3. ChatClient registers the `NinjaTools` functions
4. AI analyzes the question and determines if it needs to call any tools
5. If needed, AI invokes the appropriate tool (inflation, currency, or stock)
6. Tool makes HTTP request to API Ninjas and returns structured data
7. AI processes the tool response and generates a natural language answer
8. Service returns the answer to the client

## Architecture Highlights

- **Separation of Concerns**: Clear layering with Controllers, Services, and Tools
- **Dependency Injection**: Spring's autowiring for loose coupling
- **Function Calling**: OpenAI's function calling capability for dynamic tool invocation
- **Error Resilience**: Try-catch blocks and custom exceptions throughout
- **Extensibility**: Easy to add new tools by creating @Tool annotated methods
- **Type Safety**: Record types for immutable, type-safe data models
