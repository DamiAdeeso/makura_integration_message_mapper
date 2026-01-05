import { useState } from 'react'
import { Card, Space, Typography, Tabs, Button, message } from 'antd'
import { CodeOutlined, CopyOutlined, CheckOutlined } from '@ant-design/icons'
import './CodeExamplesTab.css'

const { Title, Text: TypographyText, Paragraph } = Typography

export const CodeExamplesTab = () => {
  const [copiedIndex, setCopiedIndex] = useState<number | null>(null)
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/dashboard-api'
  const runtimeUrl = apiBaseUrl.replace('/dashboard-api', '').replace(':8081', ':8080')

  const copyToClipboard = (text: string, index: number) => {
    navigator.clipboard.writeText(text)
    message.success('Copied to clipboard!')
    setCopiedIndex(index)
    setTimeout(() => setCopiedIndex(null), 2000)
  }

  const curlExample = `curl -X POST "${runtimeUrl}/CREDIT_TRANSFER_PACS008" \\
  -H "Content-Type: application/json" \\
  -H "X-API-Key: your-api-key-here" \\
  -H "X-Correlation-Id: unique-request-id" \\
  -d '{
    "debtor": {
      "name": "John Doe",
      "account": {
        "iban": "GB82WEST12345698765432"
      }
    },
    "creditor": {
      "name": "Jane Smith",
      "account": {
        "iban": "GB29NWBK60161331926819"
      }
    },
    "amount": {
      "value": "1000.00",
      "currency": "EUR"
    },
    "remittanceInformation": "Payment for services"
  }'`

  const javascriptExample = `// Using fetch API
const response = await fetch('${runtimeUrl}/CREDIT_TRANSFER_PACS008', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': 'your-api-key-here',
    'X-Correlation-Id': 'unique-request-id-' + Date.now()
  },
  body: JSON.stringify({
    debtor: {
      name: 'John Doe',
      account: {
        iban: 'GB82WEST12345698765432'
      }
    },
    creditor: {
      name: 'Jane Smith',
      account: {
        iban: 'GB29NWBK60161331926819'
      }
    },
    amount: {
      value: '1000.00',
      currency: 'EUR'
    },
    remittanceInformation: 'Payment for services'
  })
})

const data = await response.json()
console.log('Response:', data)`

  const pythonExample = `import requests

url = "${runtimeUrl}/CREDIT_TRANSFER_PACS008"
headers = {
    "Content-Type": "application/json",
    "X-API-Key": "your-api-key-here",
    "X-Correlation-Id": "unique-request-id"
}

payload = {
    "debtor": {
        "name": "John Doe",
        "account": {
            "iban": "GB82WEST12345698765432"
        }
    },
    "creditor": {
        "name": "Jane Smith",
        "account": {
            "iban": "GB29NWBK60161331926819"
        }
    },
    "amount": {
        "value": "1000.00",
        "currency": "EUR"
    },
    "remittanceInformation": "Payment for services"
}

response = requests.post(url, json=payload, headers=headers)
print("Status Code:", response.status_code)
print("Response:", response.json())`

  const javaExample = `import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("${runtimeUrl}/CREDIT_TRANSFER_PACS008"))
    .header("Content-Type", "application/json")
    .header("X-API-Key", "your-api-key-here")
    .header("X-Correlation-Id", "unique-request-id")
    .POST(HttpRequest.BodyPublishers.ofString("""
        {
          "debtor": {
            "name": "John Doe",
            "account": {
              "iban": "GB82WEST12345698765432"
            }
          },
          "creditor": {
            "name": "Jane Smith",
            "account": {
              "iban": "GB29NWBK60161331926819"
            }
          },
          "amount": {
            "value": "1000.00",
            "currency": "EUR"
          },
          "remittanceInformation": "Payment for services"
        }
        """))
    .build();

HttpResponse<String> response = client.send(request, 
    HttpResponse.BodyHandlers.ofString());
System.out.println("Status: " + response.statusCode());
System.out.println("Response: " + response.body());`

  const examples = [
    { name: 'cURL', code: curlExample, language: 'bash' },
    { name: 'JavaScript', code: javascriptExample, language: 'javascript' },
    { name: 'Python', code: pythonExample, language: 'python' },
    { name: 'Java', code: javaExample, language: 'java' },
  ]

  return (
    <div className="code-examples-tab">
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Title level={3}>
              <CodeOutlined /> Code Examples
            </Title>
            <Paragraph>
              Copy and paste these examples into your application. Remember to replace{' '}
              <TypographyText code>your-api-key-here</TypographyText> with your actual API key.
            </Paragraph>
          </Space>
        </Card>

        {examples.map((example, index) => (
          <Card
            key={example.name}
            title={
              <Space>
                <CodeOutlined />
                <span>{example.name}</span>
              </Space>
            }
            extra={
              <Button
                type="text"
                icon={copiedIndex === index ? <CheckOutlined /> : <CopyOutlined />}
                onClick={() => copyToClipboard(example.code, index)}
              >
                {copiedIndex === index ? 'Copied!' : 'Copy'}
              </Button>
            }
          >
            <pre className="code-block">
              <code>{example.code}</code>
            </pre>
          </Card>
        ))}

        <Card>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Title level={4}>Environment Variables</Title>
            <Paragraph>
              For production applications, use environment variables to store your API key:
            </Paragraph>
            <div style={{ marginTop: 8 }}>
              <TypographyText strong>Bash/Linux:</TypographyText>
              <pre className="code-block">
                <code>export MAKURA_API_KEY="your-api-key-here"</code>
              </pre>
            </div>
            <div style={{ marginTop: 16 }}>
              <TypographyText strong>Windows PowerShell:</TypographyText>
              <pre className="code-block">
                <code>$env:MAKURA_API_KEY="your-api-key-here"</code>
              </pre>
            </div>
            <div style={{ marginTop: 16 }}>
              <TypographyText strong>.env file:</TypographyText>
              <pre className="code-block">
                <code>MAKURA_API_KEY=your-api-key-here</code>
              </pre>
            </div>
          </Space>
        </Card>
      </Space>
    </div>
  )
}



