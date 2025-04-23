import React, { useEffect, useState } from 'react';
import { Modal, Form, Select, Input, Button, DatePicker, InputNumber, Spin, message } from 'antd';
import dayjs from 'dayjs';
import * as api from '../../api';

const { Option } = Select;
const { TextArea } = Input;

const ReviewForm = ({ visible, onCreate, onUpdate, onCancel, editingReview }) => {
  const [form] = Form.useForm();
  const [users, setUsers] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const isEditing = !!editingReview;

  useEffect(() => {
    const loadFormData = async () => {
      if (!visible) return;
      setLoading(true);
      try {
        const [usersRes, teachersRes, subjectsRes] = await Promise.all([
          api.getUsers(),
          api.getTeachers(),
          api.getSubjects(),
        ]);
        setUsers(usersRes.data);
        setTeachers(teachersRes.data);
        setSubjects(subjectsRes.data);
      } catch (error) {
        console.error("Failed to load data for review form:", error);
        messageApi.error('Не удалось загрузить данные для формы отзыва.');
      } finally {
        setLoading(false);
      }
    };
    loadFormData();
  }, [visible, messageApi]);

  useEffect(() => {
    if (visible && isEditing) {
      if (editingReview.authorId === undefined || editingReview.subjectId === undefined) {
        console.error("Editing review is missing authorId or subjectId:", editingReview);
        messageApi.error("Ошибка загрузки данных для редактирования.");
        form.resetFields();
        return;
      }
      form.setFieldsValue({
        userId: editingReview.authorId,
        teacherId: editingReview.teacher?.id,
        subjectId: editingReview.subjectId,
        date: editingReview.date ? dayjs(editingReview.date, 'YYYY-MM-DD') : null,
        grade: editingReview.grade,
        comment: editingReview.comment,
      });
    } else if (visible && !isEditing) {
      form.resetFields();
      form.setFieldsValue({ date: dayjs() });
    }
  }, [editingReview, form, isEditing, visible, messageApi]);


  const handleOk = () => {
    form
        .validateFields()
        .then((values) => {
          if (isEditing) {
            onUpdate(editingReview.id, values);
          } else {
            onCreate(values);
          }
        })
        .catch((info) => {
          console.log('Validate Failed:', info);
        });
  };

  const getTeacherFullName = (teacher) => {
    if (!teacher) return '';
    return `${teacher.surname} ${teacher.name} ${teacher.patronym || ''}`.trim();
  }

  return (
      <>
        {contextHolder}
        <Modal
            open={visible}
            title={isEditing ? 'Редактировать отзыв' : 'Добавить отзыв'}
            okText={isEditing ? 'Сохранить' : 'Создать'}
            cancelText="Отмена"
            onCancel={onCancel}
            onOk={handleOk}
            confirmLoading={loading}
            destroyOnClose
            width={700}
        >
          <Spin spinning={loading} tip="Загрузка данных...">
            <Form form={form} layout="vertical" name="review_form">
              <Form.Item
                  name="userId"
                  label="Автор (Пользователь)"
                  rules={[{ required: true, message: 'Выберите автора!' }]}
              >
                <Select
                    placeholder="Выберите пользователя"
                    showSearch
                    filterOption={(input, option) => (option?.children ?? '').toLowerCase().includes(input.toLowerCase())}
                    disabled={loading}
                >
                  {users.map(user => (
                      <Option key={user.id} value={user.id}>{user.username}</Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                  name="teacherId"
                  label="Преподаватель"
                  rules={[{ required: true, message: 'Выберите преподавателя!' }]}
              >
                <Select
                    placeholder="Выберите преподавателя"
                    showSearch
                    filterOption={(input, option) => (option?.children ?? '').toLowerCase().includes(input.toLowerCase())}
                    disabled={loading}
                >
                  {teachers.map(teacher => (
                      <Option key={teacher.id} value={teacher.id}>{getTeacherFullName(teacher)}</Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                  name="subjectId"
                  label="Предмет"
                  rules={[{ required: true, message: 'Выберите предмет!' }]}
              >
                <Select
                    placeholder="Выберите предмет"
                    showSearch
                    filterOption={(input, option) => (option?.children ?? '').toLowerCase().includes(input.toLowerCase())}
                    disabled={loading}
                >
                  {subjects.map(subject => (
                      <Option key={subject.id} value={subject.id}>{subject.name}</Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                  name="date"
                  label="Дата отзыва"
                  rules={[{ required: true, message: 'Выберите дату!' }]}
              >
                <DatePicker style={{ width: '100%' }} placeholder="Выберите дату" format="YYYY-MM-DD" disabled={loading} />
              </Form.Item>

              <Form.Item
                  name="grade"
                  label="Оценка (1-10)"
                  rules={[{ required: true, message: 'Укажите оценку!' }]}
              >
                <InputNumber min={1} max={10} style={{ width: '100%' }} placeholder="От 1 до 10" disabled={loading} />
              </Form.Item>

              <Form.Item
                  name="comment"
                  label="Комментарий"
                  rules={[{ max: 5000, message: 'Максимум 5000 символов' }]}
              >
                <TextArea rows={4} placeholder="Ваш комментарий..." disabled={loading} />
              </Form.Item>
            </Form>
          </Spin>
        </Modal>
      </>
  );
};

export default ReviewForm;