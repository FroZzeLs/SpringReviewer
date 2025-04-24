import React, { useEffect } from 'react';
import { Modal, Form, Input, Button } from 'antd';

const SubjectForm = ({ visible, onCreate, onUpdate, onCancel, editingSubject }) => {
  const [form] = Form.useForm();
  const isEditing = !!editingSubject;

  useEffect(() => {
    if (isEditing) {
      form.setFieldsValue({ name: editingSubject.name });
    } else {
      form.resetFields();
    }
  }, [editingSubject, form, isEditing]);

  const handleOk = () => {
    form
      .validateFields()
      .then((values) => {
        if (isEditing) {
          onUpdate(editingSubject.id, values);
        } else {
          onCreate(values);
        }
      })
      .catch((info) => {
        console.log('Validate Failed:', info);
      });
  };

  return (
    <Modal
      open={visible}
      title={isEditing ? 'Редактировать предмет' : 'Добавить предмет'}
      okText={isEditing ? 'Сохранить' : 'Создать'}
      cancelText="Отмена"
      onCancel={onCancel}
      onOk={handleOk}
      destroyOnClose
    >
      <Form form={form} layout="vertical" name="subject_form">
        <Form.Item
          name="name"
          label="Название предмета"
          rules={[
            { required: true, message: 'Пожалуйста, введите название!' },
            { min: 2, message: 'Минимум 2 символа' },
            { max: 100, message: 'Максимум 100 символов' },
          ]}
        >
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default SubjectForm;