import { Modal, Form, Input, message } from 'antd'
import { useMutation } from '@tanstack/react-query'
import {
  mappingsApi,
  FieldMappingDTO,
} from '../../api/mappingsApi'
import { useEffect } from 'react'

const { TextArea } = Input

interface EditMappingModalProps {
  visible: boolean
  mapping: FieldMappingDTO
  onClose: () => void
  onSuccess: () => void
}

export const EditMappingModal = ({
  visible,
  mapping,
  onClose,
  onSuccess,
}: EditMappingModalProps) => {
  const [form] = Form.useForm()

  useEffect(() => {
    if (mapping) {
      form.setFieldsValue({
        sourcePath: mapping.sourcePath,
        targetPath: mapping.targetPath,
        transform: mapping.transform || mapping.transformation,
        defaultValue: mapping.defaultValue,
      })
    }
  }, [mapping, form])

  const updateMutation = useMutation({
    mutationFn: (data: Partial<FieldMappingDTO>) =>
      mappingsApi.update(mapping.id, data),
    onSuccess: () => {
      message.success('Mapping updated successfully')
      onSuccess()
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to update mapping')
    },
  })

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      updateMutation.mutate(values)
    } catch (error) {
      console.error('Validation failed:', error)
    }
  }

  return (
    <Modal
      title={`Edit Mapping: ${mapping.sourcePath} → ${mapping.targetPath}`}
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={updateMutation.isPending}
      width={700}
      okText="Update"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="sourcePath"
          label="Source Field Path"
          rules={[{ required: true, message: 'Please enter source field path' }]}
          tooltip="Field path (e.g., source.TSQuerySingleRequest.SessionID) or constant value (e.g., constant:111444)"
          extra="Use 'constant:value' for hardcoded values"
        >
          <Input placeholder="source.TSQuerySingleRequest.SessionID or constant:111444" />
        </Form.Item>

        <Form.Item
          name="targetPath"
          label="Target Field Path"
          rules={[{ required: true, message: 'Please enter target field path' }]}
        >
          <Input placeholder="iso:Document/FIToFIPmtStsReq/GrpHdr/MsgId" />
        </Form.Item>

        <Form.Item
          name="transform"
          label="Transformation Expression"
          tooltip="Optional transformation expression. Examples: formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ'), concat('prefix', value, 'suffix')"
          extra={
            <div style={{ fontSize: '12px', color: '#666' }}>
              <div>Common: formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ'), concat('999999', formatDateTime(now(), 'yyyyMMddHHmmss'), substring(SessionID, -15)), mapStatusToResponseCode(GrpSts)</div>
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
          tooltip="Used when source is null/empty. Different from constant values (use constant:value in source field)"
        >
          <Input placeholder="Default value if source field is missing" />
        </Form.Item>
      </Form>
    </Modal>
  )
}

