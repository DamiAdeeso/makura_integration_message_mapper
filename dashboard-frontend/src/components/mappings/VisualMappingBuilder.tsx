import { useState, useEffect } from 'react'
import {
  Card,
  Row,
  Col,
  Input,
  Select,
  Button,
  Space,
  message,
  Tree,
  Empty,
  Tag,
  Divider,
} from 'antd'
import {
  ArrowRightOutlined,
  PlusOutlined,
  DeleteOutlined,
  SaveOutlined,
} from '@ant-design/icons'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { mappingsApi, FieldMappingDTO } from '../../api/mappingsApi'
import './VisualMappingBuilder.css'

const { TextArea } = Input

interface VisualMappingBuilderProps {
  mapping?: FieldMappingDTO | null
}

interface MappingRule {
  id: string
  sourcePath: string
  targetPath: string
  transform?: string
  defaultValue?: string
}

// Sample JSON structures for demonstration
const SAMPLE_SOURCE_STRUCTURE = {
  customer: {
    id: 'string',
    name: 'string',
    email: 'string',
    address: {
      street: 'string',
      city: 'string',
      country: 'string',
    },
  },
  transaction: {
    amount: 'number',
    currency: 'string',
    date: 'string',
    reference: 'string',
  },
}

const SAMPLE_ISO_FIELDS = [
  '/Document/CstmrCdtTrfInitn/PmtInf/Dbtr/Nm',
  '/Document/CstmrCdtTrfInitn/PmtInf/Dbtr/PstlAdr/Ctry',
  '/Document/CstmrCdtTrfInitn/PmtInf/CdtTrfTxInf/Amt',
  '/Document/CstmrCdtTrfInitn/PmtInf/CdtTrfTxInf/Cdtr/Nm',
  '/Document/CstmrCdtTrfInitn/GrpHdr/MsgId',
  '/Document/CstmrCdtTrfInitn/GrpHdr/CreDtTm',
]

export const VisualMappingBuilder = ({ mapping }: VisualMappingBuilderProps) => {
  const [routeId, setRouteId] = useState(mapping?.routeId || '')
  const [selectedSource, setSelectedSource] = useState('')
  const [selectedTarget, setSelectedTarget] = useState('')
  const [mappingRules, setMappingRules] = useState<MappingRule[]>([])
  const [transform, setTransform] = useState('')
  const [defaultValue, setDefaultValue] = useState('')

  const queryClient = useQueryClient()

  const saveMutation = useMutation({
    mutationFn: async (rules: MappingRule[]) => {
      const promises = rules.map((rule) =>
        mappingsApi.create({
          routeId,
          sourcePath: rule.sourcePath,
          targetPath: rule.targetPath,
          transform: rule.transform,
          defaultValue: rule.defaultValue,
        })
      )
      return Promise.all(promises)
    },
    onSuccess: () => {
      message.success('Mappings saved successfully')
      queryClient.invalidateQueries({ queryKey: ['mappings'] })
      setMappingRules([])
    },
    onError: () => {
      message.error('Failed to save mappings')
    },
  })

  const handleAddMapping = () => {
    if (!selectedSource || !selectedTarget) {
      message.warning('Please select both source and target fields')
      return
    }

    if (!routeId) {
      message.warning('Please enter Route ID')
      return
    }

    const newRule: MappingRule = {
      id: Date.now().toString(),
      sourcePath: selectedSource,
      targetPath: selectedTarget,
      transform: transform || undefined,
      defaultValue: defaultValue || undefined,
    }

    setMappingRules([...mappingRules, newRule])
    setSelectedSource('')
    setSelectedTarget('')
    setTransform('')
    setDefaultValue('')
    message.success('Mapping rule added')
  }

  const handleRemoveMapping = (id: string) => {
    setMappingRules(mappingRules.filter((rule) => rule.id !== id))
  }

  const handleSaveAll = () => {
    if (mappingRules.length === 0) {
      message.warning('No mapping rules to save')
      return
    }
    saveMutation.mutate(mappingRules)
  }

  // Build tree data from sample structure
  const buildTreeData = (obj: any, prefix = '$'): any[] => {
    return Object.entries(obj).map(([key, value]) => {
      const path = `${prefix}.${key}`
      if (typeof value === 'object' && !Array.isArray(value)) {
        return {
          title: `${key} (object)`,
          key: path,
          children: buildTreeData(value, path),
        }
      }
      return {
        title: `${key}: ${value}`,
        key: path,
        isLeaf: true,
      }
    })
  }

  const sourceTreeData = buildTreeData(SAMPLE_SOURCE_STRUCTURE)

  return (
    <div className="visual-mapping-builder">
      <Card title="Route Configuration" size="small" style={{ marginBottom: 16 }}>
        <Input
          placeholder="Enter Route ID (e.g., SYSTEM_TO_NIP)"
          value={routeId}
          onChange={(e) => setRouteId(e.target.value)}
          style={{ maxWidth: 400 }}
        />
      </Card>

      <Row gutter={16}>
        {/* Source Fields */}
        <Col xs={24} md={10}>
          <Card
            title="Source Fields (Your System)"
            size="small"
            className="builder-card"
          >
            <div className="field-selector">
              <Tree
                treeData={sourceTreeData}
                onSelect={(keys) => {
                  if (keys.length > 0) {
                    setSelectedSource(keys[0] as string)
                  }
                }}
                selectedKeys={selectedSource ? [selectedSource] : []}
                defaultExpandAll
              />
            </div>
            <Divider />
            <Input
              placeholder="Or type custom path: source.Element.SubElement or constant:value"
              value={selectedSource}
              onChange={(e) => setSelectedSource(e.target.value)}
              addonBefore="Selected:"
            />
            <div style={{ fontSize: '11px', color: '#666', marginTop: 4 }}>
              Tip: Use "constant:value" for hardcoded values
            </div>
          </Card>
        </Col>

        {/* Mapping Controls */}
        <Col xs={24} md={4}>
          <Card size="small" className="mapping-controls">
            <Space direction="vertical" style={{ width: '100%' }}>
              <TextArea
                placeholder="Transform expression (optional)"
                value={transform}
                onChange={(e) => setTransform(e.target.value)}
                rows={3}
                style={{ fontSize: '11px' }}
              />
              <Input
                placeholder="Default value (optional)"
                value={defaultValue}
                onChange={(e) => setDefaultValue(e.target.value)}
                size="small"
              />
              <Button
                type="primary"
                icon={<ArrowRightOutlined />}
                onClick={handleAddMapping}
                block
              >
                Add Mapping
              </Button>
            </Space>
          </Card>
        </Col>

        {/* Target Fields */}
        <Col xs={24} md={10}>
          <Card
            title="Target Fields (ISO 20022)"
            size="small"
            className="builder-card"
          >
            <div className="field-selector">
              <div className="iso-field-list">
                {SAMPLE_ISO_FIELDS.map((field) => (
                  <div
                    key={field}
                    className={`iso-field-item ${
                      selectedTarget === field ? 'selected' : ''
                    }`}
                    onClick={() => setSelectedTarget(field)}
                  >
                    <code>{field}</code>
                  </div>
                ))}
              </div>
            </div>
            <Divider />
            <Input
              placeholder="Or type custom ISO path: iso:Document/Element/SubElement"
              value={selectedTarget}
              onChange={(e) => setSelectedTarget(e.target.value)}
              addonBefore="Selected:"
            />
          </Card>
        </Col>
      </Row>

      {/* Mapping Rules Summary */}
      <Card
        title={`Mapping Rules (${mappingRules.length})`}
        size="small"
        style={{ marginTop: 16 }}
        extra={
          <Button
            type="primary"
            icon={<SaveOutlined />}
            onClick={handleSaveAll}
            loading={saveMutation.isPending}
            disabled={mappingRules.length === 0}
          >
            Save All Mappings
          </Button>
        }
      >
        {mappingRules.length === 0 ? (
          <Empty description="No mapping rules added yet" />
        ) : (
          <div className="mapping-rules-list">
            {mappingRules.map((rule) => (
              <div key={rule.id} className="mapping-rule-item">
                <div className="rule-content">
                  <code className="source-field">{rule.sourcePath}</code>
                  <ArrowRightOutlined className="arrow-icon" />
                  {rule.transform && (
                    <>
                      <Tag color="purple" title={rule.transform}>
                        {rule.transform.length > 20 ? rule.transform.substring(0, 20) + '...' : rule.transform}
                      </Tag>
                      <ArrowRightOutlined className="arrow-icon" />
                    </>
                  )}
                  {rule.defaultValue && (
                    <>
                      <Tag color="green">Default: {rule.defaultValue}</Tag>
                      <ArrowRightOutlined className="arrow-icon" />
                    </>
                  )}
                  <code className="target-field">{rule.targetPath}</code>
                </div>
                <Button
                  type="text"
                  danger
                  size="small"
                  icon={<DeleteOutlined />}
                  onClick={() => handleRemoveMapping(rule.id)}
                />
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  )
}

