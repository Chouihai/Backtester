# Stock Trading Backtester

A JavaFX-based stock trading strategy backtester application that allows you to write and test trading strategies using a custom scripting language.

## Overview

This application provides a graphical interface for backtesting stock trading strategies. It features:

- **Custom Scripting Language**: Write trading strategies using a simple, Python-like syntax
- **Flexible Data Sources**: Fetch historical data from Alpha Vantage API or use local JSON files
- **Performance Metrics**: Track profit/loss, drawdown, run-up, and Sharpe ratio
- **Trade Management**: Visualize trades and positions with detailed analytics
- **Configuration Management**: Flexible configuration for data sources and API keys

## Prerequisites

- **Java 22** (or later)
- **Maven 3.6+**
- **IntelliJ IDEA** (recommended) or any Java IDE
- **Alpha Vantage API Key** (optional, for live data)

## Setup and Installation

### 1. Clone the Repository and Configure the Application

There is a `backtester-config.properties` file in the project root or `src/main/resources/`:

```properties
# Data source: "api" for Alpha Vantage, "file" for local JSON
dataSource=file

# File path for local data (relative to resources or absolute path)
filePath=AAPL.JSON

# Alpha Vantage API key (required if dataSource=api)
apiKey=your_api_key_here
```

### 2. Build the Project
```bash
mvn clean compile
```

## Running the Application

### Option 1: Command Line
```bash
# Compile and run
mvn clean compile exec:java -Dexec.mainClass="Backtester.ui.BacktesterApplication"

# Or use JavaFX Maven plugin
mvn javafx:run
```

### Option 2: IntelliJ IDEA

1. **Open the Project**:
   - Open IntelliJ IDEA
   - Select "Open" and choose the `HaitamStockProject` folder
   - Wait for Maven to download dependencies

2. **Configure Run Configuration**:
   - Go to `Run` → `Edit Configurations...`
   - Click the `+` button and select `Application`
   - Configure as follows:
     - **Name**: `BacktesterApplication`
     - **Main class**: `Backtester.ui.BacktesterApplication`
     - **Module**: `backtester`
     - **VM options**: ```--add-modules
       javafx.controls,javafx.fxml --enable-native-access=ALL-UNNAMED```
     - **Working directory**: `$MODULE_DIR$`

3. **Run the Application**:
   - Click the green "Run" button or press `Shift + F10`
   - The JavaFX application window should appear

## Scripting Language

The application uses a custom scripting language with Python syntax for writing trading strategies.

### Basic Syntax

```python
// variable assignment
sma20 = sma(20)

// conditional statements
if close() > open():
    createOrder("Long", true, 100)
else crossover(sma20, sma50):
    createOrder("Golden Cross Long", true, 1000)
```

### Available Functions

#### `sma(days)`
Calculates Simple Moving Average over the specified number of days.
- **Arguments**: 1 (number of days)
- **Returns**: ValueAccumulator
- **Example**: `sma20 = sma(20)`

#### `crossover(value1, value2)`
Detects when the first value crosses above the second value.
- **Arguments**: 2 (two ValueAccumulators)
- **Returns**: Boolean ValueAccumulator (evaluates to the most recent value)
- **Example**: `crossover(sma20, sma50)`

#### `createOrder(name, isBuy, quantity)`
Creates a new trading order.
- **Arguments**: 3 (order name, buy/sell flag, quantity)
- **Returns**: void
- **Example**: `createOrder("Long", true, 1000)`

[//]: # (#### `closeOrder&#40;orderId&#41;`)

[//]: # (Closes an existing order.)

[//]: # (- **Arguments**: 1 &#40;order ID&#41;)

[//]: # (- **Returns**: void)

[//]: # (- **Example**: `closeOrder&#40;"position1"&#41;`)

### Complete Strategy Example

```python
sma20 = sma(20)
sma50 = sma(50)

if crossover(sma20, sma50):
    createOrder("Golden Cross Long", true, 1000)

if :crossover(sma50, sma20):
    createOrder("Death Cross Short", false, 1000)
```

## Configuration Options

### Data Sources

#### File-based Data
The application supports JSON. Your JSON objects should have:
- Date strings as keys (ISO format: `YYYY-MM-DD`)
- OHLCV (Open, High, Low, Close, Volume) data

**Example JSON format:**
```json
{
  "2024-01-01": {
    "open": 100.50,
    "high": 105.20,
    "low": 99.80,
    "close": 103.45,
    "volume": 1000000
  },
  "2024-01-02": {
    "o": 103.45,
    "h": 107.30,
    "l": 102.90,
    "c": 106.20,
    "v": 1200000
  }
}
```

```properties
dataSource=file
filePath=AAPL.JSON
```

#### API Data (Alpha Vantage)
```properties
dataSource=api
apiKey=your_alpha_vantage_api_key
```

### Database Configuration (Optional)
```properties
db.url=jdbc:postgresql://localhost:5432/stockdb
db.username=postgres
db.password=your_password
```

## Performance Metrics

The application tracks several key performance indicators:

- **Gross Profit**: Total profit from winning trades
- **Gross Loss**: Total loss from losing trades
- **Net Profit**: Gross profit minus gross loss
- **Open P&L**: Current unrealized profit/loss

[//]: # (- **Drawdown**: Maximum peak-to-trough decline)

[//]: # (- **Run-up**: Maximum trough-to-peak gain)

[//]: # (- **Sharpe Ratio**: Risk-adjusted return measure)

## Troubleshooting

### Common Issues

1. **JavaFX Not Found**:
   - Ensure Java 22+ is installed
   - Add `--enable-preview` to VM options

2. **API Key Issues**:
   - Verify your Alpha Vantage API key is valid
   - Check API rate limits (5 calls per minute for free tier)

3. **Data File Not Found**:
   - Ensure your JSON file exists in the specified path
   - Check file path in configuration
   - Verify JSON format has date keys and OHLCV data

4. **Compilation Errors**:
   - Run `mvn clean compile` to rebuild
   - Check Java version compatibility