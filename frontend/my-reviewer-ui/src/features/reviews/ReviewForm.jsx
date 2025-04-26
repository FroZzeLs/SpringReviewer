import React, { useEffect, useState, useCallback } from 'react';
import { Modal, Form, Select, Input, Button, DatePicker, Spin, message } from 'antd';
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
  const [gradeInputValue, setGradeInputValue] = useState('');

  const loadSelectData = useCallback(async () => {
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
  }, [messageApi]);

  useEffect(() => {
    if (visible) {
      loadSelectData();
      if (isEditing) {
        if (editingReview.authorId === undefined || editingReview.subjectId === undefined) {
          console.error("Editing review is missing authorId or subjectId:", editingReview);
          messageApi.error("Ошибка загрузки данных для редактирования.");
          form.resetFields();
          setGradeInputValue('');
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
        setGradeInputValue(editingReview.grade !== null && editingReview.grade !== undefined ? String(editingReview.grade) : '');
      } else {
        form.resetFields();
        setGradeInputValue('');
        form.setFieldsValue({ date: dayjs() });
      }
    } else {
      form.resetFields();
      setGradeInputValue('');
    }
  }, [visible, editingReview, isEditing, form, messageApi, loadSelectData]);

  const handleOk = useCallback(() => {
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
          // Find first error field and focus it
          if (info.errorFields && info.errorFields.length > 0) {
            const errorFieldName = info.errorFields[0].name[0];
            form.scrollToField(errorFieldName);
          }
        });
  }, [form, isEditing, editingReview, onCreate, onUpdate]);

  const getTeacherFullName = useCallback((teacher) => {
    if (!teacher) return '';
    return `${teacher.surname} ${teacher.name} ${teacher.patronym || ''}`.trim();
  }, []);

  const handleGradeInputChange = (event) => {
    const rawValue = event.target.value;
    setGradeInputValue(rawValue);

    const digits = rawValue.replace(/[^\d]/g, '');
    const numericValue = digits === '' ? null : parseInt(digits, 10);

    form.setFieldsValue({ grade: numericValue });
    form.validateFields(['grade']);
  };

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (visible && (event.ctrlKey || event.metaKey) && event.key === 'Enter') {
        event.preventDefault();
        handleOk();
      }
    };

    if (visible) {
      window.addEventListener('keydown', handleKeyDown);
    }

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [visible, handleOk]);


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
            maskClosable={false}
        >
          <Spin spinning={loading} tip="Загрузка данных...">
            <Form form={form} layout="vertical" name="review_form" autoComplete="off">
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
                  rules={[
                    { required: true, message: 'Укажите оценку!' },
                    {
                      validator: async (_, value) => {
                        if (value === null || value === undefined) {
                          return Promise.resolve();
                        }
                        const numValue = Number(value);
                        if (isNaN(numValue)) {
                          return Promise.reject(new Error('Введите число!'));
                        }
                        if (numValue < 1 || numValue > 10) {
                          return Promise.reject(new Error('Оценка должна быть от 1 до 10!'));
                        }
                        return Promise.resolve();
                      }
                    }
                  ]}
                  validateTrigger="onChange"
              >
                <Input
                    style={{ width: '100%' }}
                    placeholder="От 1 до 10"
                    disabled={loading}
                    value={gradeInputValue}
                    onChange={handleGradeInputChange}
                    maxLength={2}
                />
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