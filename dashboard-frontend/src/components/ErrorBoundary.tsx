import React, { Component, ReactNode } from 'react'
import { Result, Button } from 'antd'
import { FrownOutlined } from '@ant-design/icons'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = {
      hasError: false,
      error: null,
    }
  }

  static getDerivedStateFromError(error: Error): State {
    return {
      hasError: true,
      error,
    }
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Error Boundary caught an error:', error, errorInfo)
    // You can also log the error to an error reporting service here
  }

  handleReset = () => {
    this.setState({
      hasError: false,
      error: null,
    })
    window.location.href = '/'
  }

  render() {
    if (this.state.hasError) {
      return (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100vh',
            padding: '24px',
          }}
        >
          <Result
            status="error"
            icon={<FrownOutlined />}
            title="Oops! Something went wrong"
            subTitle={
              <>
                <p>We're sorry, but an unexpected error occurred.</p>
                {process.env.NODE_ENV === 'development' && this.state.error && (
                  <pre
                    style={{
                      textAlign: 'left',
                      padding: '16px',
                      background: '#f5f5f5',
                      borderRadius: '4px',
                      marginTop: '16px',
                      fontSize: '12px',
                      overflow: 'auto',
                      maxWidth: '600px',
                    }}
                  >
                    {this.state.error.message}
                    {'\n\n'}
                    {this.state.error.stack}
                  </pre>
                )}
              </>
            }
            extra={[
              <Button type="primary" key="reset" onClick={this.handleReset}>
                Go to Dashboard
              </Button>,
              <Button
                key="reload"
                onClick={() => window.location.reload()}
              >
                Reload Page
              </Button>,
            ]}
          />
        </div>
      )
    }

    return this.props.children
  }
}



