import { Modal, Form, Input, Select, Switch, message } from 'antd'
import { useMutation } from '@tanstack/react-query'
import { mappingsApi, CreateFieldMappingRequest } from '../../api/mappingsApi'

const { TextArea } = Input

interface CreateMappingModalProps {
  visible: boolean
  onClose: () => void
  onSuccess: () => void
}

export const CreateMappingModal = ({
  visible,
  onClose,
  onSuccess,
}: CreateMappingModalProps) => {
  const [form] = Form.useForm()

  const createMutation = useMutation({
    mutationFn: mappingsApi.create,
    onSuccess: () => {
      message.success('Mapping created successfully')
      form.resetFields()
      onSuccess()
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to create mapping')
    },
  })

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      createMutation.mutate(values)
    } catch (error) {
      console.error('Validation failed:', error)
    }
  }

  return (
    <Modal
      title="Create New Mapping"
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={createMutation.isPending}
      width={700}
      okText="Create"
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          required: false,
        }}
      >
        <Form.Item
          name="routeId"
          label="Route ID"
          rules={[{ required: true, message: 'Please enter Route ID' }]}
          tooltip="The route this mapping belongs to"
        >
          <Input placeholder="SYSTEM_TO_NIP" />
        </Form.Item>

        <Form.Item
          name="sourcePath"
          label="Source Field Path"
          rules={[{ required: true, message: 'Please enter source field path' }]}
          tooltip="Field path (e.g., source.TSQuerySingleRequest.SessionID) or constant value (e.g., constant:111444 or constant:pacs.008.001.12)"
          extra="Use 'constant:value' for hardcoded values. For XML, use dot notation like 'source.Element.SubElement'"
        >
          <Input placeholder="source.TSQuerySingleRequest.SessionID or constant:111444" />
        </Form.Item>

        <Form.Item
          name="targetPath"
          label="Target Field Path"
          rules={[{ required: true, message: 'Please enter target field path' }]}
          tooltip="ISO 20022 field path, e.g., /Document/CstmrCdtTrfInitn/PmtInf/Dbtr/Nm"
        >
          <Input placeholder="/Document/CstmrCdtTrfInitn/PmtInf/Dbtr/Nm" />
        </Form.Item>

        <Form.Item
          name="transform"
          label="Transformation Expression"
          tooltip="Optional transformation expression. Examples: formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ'), concat('prefix', value, 'suffix'), mapStatusToResponseCode(GrpSts)"
          extra={
            <div style={{ fontSize: '12px', color: '#666' }}>
              <div>Common transformations:</div>
              <div>• formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ') - Current timestamp</div>
              <div>• concat('999999', formatDateTime(now(), 'yyyyMMddHHmmss'), substring(SessionID, -15)) - Concatenate with timestamp</div>
              <div>• subtractDays(now(), 6) - Date 6 days ago</div>
              <div>• mapStatusToResponseCode(GrpSts) - Map status codes (ACSC→25, RJCT→99)</div>
              <div>• substring(value, -15) - Last 15 characters</div>
            </div>
          }
        >
          <TextArea
            rows={3}
            placeholder="formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ')"
          />
        </Form.Item>

        <Form.Item 
          name="defaultValue" 
          label="Default Value" 
          tooltip="Used when source is null/empty. This is different from constant values (use constant:value in source field instead)"
        >
          <Input placeholder="Default value if source field is missing" />
        </Form.Item>

        <Form.Item
          name="required"
          label="Required Field"
          valuePropName="checked"
          tooltip="Translation will fail if this field is missing"
        >
          <Switch />
        </Form.Item>
      </Form>
    </Modal>
  )
}

