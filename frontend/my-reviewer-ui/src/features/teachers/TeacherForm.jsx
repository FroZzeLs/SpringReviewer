// src/features/teachers/TeacherForm.jsx
import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Button, Select, Spin, message } from 'antd';
import * as api from '../../api';

const { Option } = Select;

const TeacherForm = ({ visible, onCreate, onUpdate, onCancel, editingTeacher }) => {
  const [form] = Form.useForm();
  const [subjects, setSubjects] = useState([]);
  const [loadingSubjects, setLoadingSubjects] = useState(false);
  const [messageApi, contextHolder] = message.useMessage(); // Для локальных сообщений об ошибках
  const isEditing = !!editingTeacher;

  // Загрузка списка предметов для MultiSelect
  useEffect(() => {
    const fetchSubjects = async () => {
      setLoadingSubjects(true);
      try {
        const response = await api.getSubjects();
        setSubjects(response.data); // Ожидаем массив { id: number, name: string, teacherNames: string[] }
      } catch (error) {
         console.error("Failed to fetch subjects:", error);
         messageApi.error('Не удалось загрузить список предметов'); // Используем локальный messageApi
      } finally {
        setLoadingSubjects(false);
      }
    };
    if (visible) { // Загружаем только когда модальное окно видимо
         fetchSubjects();
    }
  }, [visible, messageApi]); // Добавили messageApi в зависимости

  // Заполнение формы при редактировании
  useEffect(() => {
    if (visible && isEditing && subjects.length > 0) { // Убедимся что предметы загружены
      // Находим ID предметов по их именам, которые есть у редактируемого учителя
       const teacherSubjectIds = editingTeacher.subjects
         .map(subjName => {
             const found = subjects.find(s => s.name === subjName);
             return found ? found.id : null; // Возвращаем ID или null если не найден
         })
         .filter(id => id !== null); // Отфильтровываем null

      form.setFieldsValue({
        surname: editingTeacher.surname,
        name: editingTeacher.name,
        patronym: editingTeacher.patronym,
        subjectIds: teacherSubjectIds, // Используем ID для Select
      });
    } else if (visible && !isEditing) {
      form.resetFields();
    }
  }, [editingTeacher, form, isEditing, visible, subjects]); // Добавили subjects

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      const teacherData = {
        surname: values.surname,
        name: values.name,
        patronym: values.patronym,
      };
      const selectedSubjectIds = values.subjectIds || []; // ID выбранных предметов

      if (isEditing) {
        await onUpdate(editingTeacher.id, teacherData, selectedSubjectIds);
      } else {
        await onCreate(teacherData, selectedSubjectIds);
      }
    } catch (info) {
      if (info.errorFields) {
          console.log('Validate Failed:', info);
      } else {
          // Ошибка пришла из onCreate/onUpdate (например, API error)
          // Она должна обрабатываться там и показывать сообщение
          console.error('Submit failed:', info);
      }
    }
  };


  return (
    <>
    {contextHolder}
    <Modal
      open={visible}
      title={isEditing ? 'Редактировать преподавателя' : 'Добавить преподавателя'}
      okText={isEditing ? 'Сохранить' : 'Создать'}
      cancelText="Отмена"
      onCancel={onCancel}
      onOk={handleOk} // Вызываем handleOk по клику OK
      confirmLoading={loadingSubjects} // Можно добавить общий лоадер на время сабмита
      destroyOnClose
      width={600} // Увеличим ширину для Select
    >
      <Spin spinning={loadingSubjects} tip="Загрузка предметов...">
          <Form form={form} layout="vertical" name="teacher_form">
            <Form.Item
              name="surname"
              label="Фамилия"
              rules={[{ required: true, message: 'Пожалуйста, введите фамилию!' }, { max: 50 }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="name"
              label="Имя"
              rules={[{ required: true, message: 'Пожалуйста, введите имя!' }, { max: 50 }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="patronym"
              label="Отчество"
              rules={[{ max: 50 }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="subjectIds"
              label="Предметы"
            >
              <Select
                mode="multiple"
                allowClear
                style={{ width: '100%' }}
                placeholder="Выберите предметы"
                loading={loadingSubjects}
                filterOption={(input, option) =>
                    option.children.toLowerCase().includes(input.toLowerCase())
                }
              >
                {subjects.map(subject => (
                  <Option key={subject.id} value={subject.id}>
                    {subject.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Form>
      </Spin>
    </Modal>
    </>
  );
};

export default TeacherForm;