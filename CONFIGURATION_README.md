# Backtester Configuration Guide

The Backtester application can be configured to use either API data or local file data for historical stock prices.

## Configuration File

The application uses a configuration file called `backtester-config.properties` in the application directory. This file is automatically created when you first run the application.

## Configuration Options

### Data Source (`data.source`)

Choose between two data sources:

- **API**: Use Alpha Vantage API for real-time historical data
- **FILE**: Use a local JSON file for historical data

### API Configuration

If using `data.source=API`:

1. **Get an API Key**: 
   - Visit [Alpha Vantage](https://www.alphavantage.co/support/#api-key)
   - Sign up for a free account
   - Copy your API key

2. **Configure the API Key**:
   ```
   api.key=YOUR_API_KEY_HERE
   ```

**Note**: The free API has rate limits (5 calls per minute, 500 calls per day).

### File Configuration

If using `data.source=FILE`:

1. **Prepare your data file**:
   - Use JSON format compatible with Alpha Vantage API response
   - Example structure:
   ```json
   {
     "2023-01-01": {
       "1. open": "150.00",
       "2. high": "152.00", 
       "3. low": "149.00",
       "4. close": "151.00",
       "5. volume": "1000000"
     }
   }
   ```

2. **Configure the file path**:
   ```
   file.path=path/to/your/data.json
   ```

## Example Configuration

### Using API:
```properties
data.source=API
api.key=YOUR_ALPHA_VANTAGE_API_KEY
file.path=data/AAPL.json
```

### Using File:
```properties
data.source=FILE
api.key=
file.path=AAPL.JSON
```

## Default Configuration

By default, the application is configured to use file-based data:
- `data.source=FILE`
- `file.path=AAPL.JSON` (from resources folder)
- `api.key=` (empty)

## Troubleshooting

### API Issues
- **"API key is not configured"**: Set a valid API key in the configuration file
- **"API rate limit exceeded"**: Wait a moment and try again, or upgrade your Alpha Vantage plan
- **"Symbol not found"**: Check that the stock symbol is valid

### File Issues
- **"No data file found"**: Check that the file path in the configuration is correct
- **"Could not load from configured path"**: Ensure the file exists and is readable

## Data File Format

The application expects JSON data in the same format as Alpha Vantage API responses. If you have data in a different format, you may need to convert it or modify the application to support your format. 