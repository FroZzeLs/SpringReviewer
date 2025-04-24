import React, { useEffect } from 'react';
import { Modal, Form, Input, Button } from 'antd';

const UserForm = ({ visible, onCreate, onUpdate, onCancel, editingUser }) => {
  const [form] = Form.useForm();
  const isEditing = !!editingUser;

  useEffect(() => {
    if (isEditing) {
      form.setFieldsValue({ username: editingUser.username });
    } else {
      form.resetFields();
    }
  }, [editingUser, form, isEditing]);

  const handleOk = () => {
    form
      .validateFields()
      .then((values) => {
        if (isEditing) {
          onUpdate(editingUser.id, values);
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
      title={isEditing ? 'Редактировать пользователя' : 'Добавить пользователя'}
      okText={isEditing ? 'Сохранить' : 'Создать'}
      cancelText="Отмена"
      onCancel={onCancel}
      onOk={handleOk}
      destroyOnClose
    >
      <Form form={form} layout="vertical" name="user_form">
        <Form.Item
          name="username"
          label="Имя пользователя (логин)"
          rules={[
            { required: true, message: 'Пожалуйста, введите имя пользователя!' },
            { min: 3, message: 'Минимум 3 символа' },
            { max: 50, message: 'Максимум 50 символов' },
          ]}
        >
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UserForm;