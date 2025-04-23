import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import { List, Button, Typography, Spin, Popconfirm, message, Card, Tag, Rate, Empty, Select, Row, Col, Space } from 'antd';
import { MessageOutlined, PlusOutlined, EditOutlined, DeleteOutlined, CalendarOutlined, UserOutlined, TeamOutlined, BookOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import * as api from '../../api';
import ReviewForm from './ReviewForm';

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;

const ReviewList = ({ mode = 'all', title }) => {
    const { userId, teacherId } = useParams();
    const [allReviews, setAllReviews] = useState([]);
    const [filteredReviews, setFilteredReviews] = useState([]);
    const [loading, setLoading] = useState(false);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingReview, setEditingReview] = useState(null);
    const [messageApi, contextHolder] = message.useMessage();
    const [entityName, setEntityName] = useState('');
    const [filterUsers, setFilterUsers] = useState([]);
    const [filterTeachers, setFilterTeachers] = useState([]);
    const [filterSubjects, setFilterSubjects] = useState([]);
    const [selectedAuthorId, setSelectedAuthorId] = useState(null);
    const [selectedTeacherId, setSelectedTeacherId] = useState(null);
    const [selectedSubjectId, setSelectedSubjectId] = useState(null);
    const [loadingFilters, setLoadingFilters] = useState(false);

    const getTeacherFullName = useCallback((teacher) => {
        if (!teacher) return 'N/A';
        return `${teacher.surname} ${teacher.name} ${teacher.patronym || ''}`.trim();
    }, []);

    const loadFilterData = useCallback(async () => {
        if (mode !== 'all' || (filterUsers.length && filterTeachers.length && filterSubjects.length)) {
            return;
        }
        setLoadingFilters(true);
        try {
            const [usersRes, teachersRes, subjectsRes] = await Promise.all([
                filterUsers.length === 0 ? api.getUsers() : Promise.resolve({ data: filterUsers }),
                filterTeachers.length === 0 ? api.getTeachers() : Promise.resolve({ data: filterTeachers }),
                filterSubjects.length === 0 ? api.getSubjects() : Promise.resolve({ data: filterSubjects }),
            ]);
            setFilterUsers(usersRes.data);
            setFilterTeachers(teachersRes.data);
            setFilterSubjects(subjectsRes.data);
        } catch (error) {
            console.error("Failed to load filter data", error);
            messageApi.error("Не удалось загрузить данные для фильтров");
        } finally {
            setLoadingFilters(false);
        }
    }, [mode, messageApi, filterUsers, filterTeachers, filterSubjects]);

    const fetchReviews = useCallback(async () => {
        setLoading(true);
        setEntityName('');
        setSelectedAuthorId(null);
        setSelectedTeacherId(null);
        setSelectedSubjectId(null);
        let pageTitle = title || "Отзывы";

        try {
            let response;

            if (mode === 'user' && userId) {
                response = await api.getReviewsByUserId(userId);
                if (response.data.length > 0 && response.data[0].author) {
                    setEntityName(response.data[0].author);
                    pageTitle = `Отзывы пользователя: ${response.data[0].author}`;
                } else {
                    try {
                        const userRes = await api.getUserById(userId);
                        setEntityName(userRes.data.username);
                        pageTitle = `Отзывы пользователя: ${userRes.data.username}`;
                    } catch (userError) {
                        console.error("Could not fetch user name", userError);
                        setEntityName(`ID: ${userId}`);
                        pageTitle = `Отзывы пользователя ID: ${userId}`;
                        if (userError?.response?.status === 404) {
                            setEntityName(`Пользователь ID: ${userId} (не найден)`);
                        }
                    }
                }

            } else if (mode === 'teacher' && teacherId) {
                response = await api.getReviewsByTeacherId(teacherId);
                if (response.data.length > 0 && response.data[0].teacher) {
                    const teacherName = getTeacherFullName(response.data[0].teacher);
                    setEntityName(teacherName);
                    pageTitle = `Отзывы о преподавателе: ${teacherName}`;
                } else {
                    try {
                        const teacherRes = await api.getTeacherById(teacherId);
                        const teacherName = getTeacherFullName(teacherRes.data);
                        setEntityName(teacherName);
                        pageTitle = `Отзывы о преподавателе: ${teacherName}`;
                    } catch (teacherError) {
                        console.error("Could not fetch teacher name", teacherError);
                        setEntityName(`ID: ${teacherId}`);
                        pageTitle = `Отзывы о преподавателе ID: ${teacherId}`;
                        if (teacherError?.response?.status === 404) {
                            setEntityName(`Преподаватель ID: ${teacherId} (не найден)`);
                        }
                    }
                }

            } else {
                response = await api.getReviews();
                pageTitle = "Все отзывы";
                loadFilterData();
            }

            const sortedReviews = response.data.sort((a, b) => {
                const surnameA = a.teacher?.surname?.toLowerCase() || '';
                const surnameB = b.teacher?.surname?.toLowerCase() || '';
                if (surnameA < surnameB) return -1;
                if (surnameA > surnameB) return 1;
                const nameA = a.teacher?.name?.toLowerCase() || '';
                const nameB = b.teacher?.name?.toLowerCase() || '';
                if (nameA < nameB) return -1;
                if (nameA > nameB) return 1;
                return 0;
            });
            setAllReviews(sortedReviews);
            setFilteredReviews(sortedReviews);
            if (!title) {
                document.title = pageTitle;
            }

        } catch (error) {
            api.handleApiError(error, messageApi);
            if (error.response && error.response.status === 404) {
                setAllReviews([]);
                setFilteredReviews([]);
                if (mode === 'user' && !entityName.includes('Найден')) setEntityName(`Пользователь ID: ${userId} (не найден или нет отзывов)`);
                if (mode === 'teacher' && !entityName.includes('Найден')) setEntityName(`Преподаватель ID: ${teacherId} (не найден или нет отзывов)`);
            }
        } finally {
            setLoading(false);
        }
    }, [mode, userId, teacherId, messageApi, getTeacherFullName, title, entityName, loadFilterData]);

    useEffect(() => {
        fetchReviews();
        return () => { document.title = 'Spring Reviewer'; }
    }, [mode, userId, teacherId]);

    useEffect(() => {
        const filteredData = allReviews.filter(review => {
            const authorMatch = !selectedAuthorId || review.authorId === selectedAuthorId;
            const teacherMatch = !selectedTeacherId || review.teacher?.id === selectedTeacherId;
            const subjectMatch = !selectedSubjectId || review.subjectId === selectedSubjectId;
            return authorMatch && teacherMatch && subjectMatch;
        });
        setFilteredReviews(filteredData);
    }, [selectedAuthorId, selectedTeacherId, selectedSubjectId, allReviews]);


    const handleAdd = () => {
        setEditingReview(null);
        setIsModalVisible(true);
    };

    const handleEdit = (review) => {
        if (review.authorId === undefined || review.subjectId === undefined) {
            console.error("Review object is missing authorId or subjectId for editing:", review);
            messageApi.error("Ошибка: Недостаточно данных для редактирования отзыва.");
            return;
        }
        setEditingReview(review);
        setIsModalVisible(true);
    };

    const handleDelete = async (id) => {
        try {
            await api.deleteReview(id);
            messageApi.success('Отзыв успешно удален');
            fetchReviews();
        } catch (error) {
            api.handleApiError(error, messageApi);
        }
    };

    const handleCreate = async (values) => {
        try {
            await api.createReview(values);
            messageApi.success('Отзыв успешно создан');
            setIsModalVisible(false);
            fetchReviews();
        } catch (error) {
            api.handleApiError(error, messageApi);
        }
    };

    const handleUpdate = async (id, values) => {
        try {
            await api.updateReview(id, values);
            messageApi.success('Отзыв успешно обновлен');
            setIsModalVisible(false);
            setEditingReview(null);
            fetchReviews();
        } catch (error) {
            api.handleApiError(error, messageApi);
        }
    };

    const handleCancel = () => {
        setIsModalVisible(false);
        setEditingReview(null);
    };

    const getPageTitle = () => {
        if (title) return title;
        const baseTitle = mode === 'user' ? 'Отзывы пользователя' : 'Отзывы о преподавателе';
        return entityName ? `${baseTitle}: ${entityName}` : (loading ? 'Загрузка...' : baseTitle);
    }

    const handleAuthorFilterChange = (value) => setSelectedAuthorId(value);
    const handleTeacherFilterChange = (value) => setSelectedTeacherId(value);
    const handleSubjectFilterChange = (value) => setSelectedSubjectId(value);


    return (
        <div className="list-container">
            {contextHolder}
            <Title level={4} className="section-title">
                { (mode === 'user' || mode === 'teacher') && (
                    <Link to={mode === 'user' ? '/users' : '/teachers'} style={{ marginRight: 15 }}>
                        <Button type="text" shape="circle" icon={<ArrowLeftOutlined />} />
                    </Link>
                )}
                {getPageTitle()}
            </Title>

            {mode === 'all' && (
                <Spin spinning={loadingFilters}>
                    <Space direction="vertical" style={{ width: '100%', marginBottom: 16 }}>
                        <Row gutter={16}>
                            <Col xs={24} md={8}>
                                <Select
                                    showSearch
                                    allowClear
                                    placeholder="Фильтр по автору"
                                    style={{ width: '100%' }}
                                    value={selectedAuthorId}
                                    onChange={handleAuthorFilterChange}
                                    filterOption={(input, option) =>
                                        (option?.children ?? '').toLowerCase().includes(input.toLowerCase())
                                    }
                                    disabled={loadingFilters || !filterUsers.length}
                                >
                                    {filterUsers.map(user => (
                                        <Option key={user.id} value={user.id}>{user.username}</Option>
                                    ))}
                                </Select>
                            </Col>
                            <Col xs={24} md={8}>
                                <Select
                                    showSearch
                                    allowClear
                                    placeholder="Фильтр по преподавателю"
                                    style={{ width: '100%' }}
                                    value={selectedTeacherId}
                                    onChange={handleTeacherFilterChange}
                                    filterOption={(input, option) =>
                                        (option?.children ?? '').toLowerCase().includes(input.toLowerCase())
                                    }
                                    disabled={loadingFilters || !filterTeachers.length}
                                >
                                    {filterTeachers.map(teacher => (
                                        <Option key={teacher.id} value={teacher.id}>{getTeacherFullName(teacher)}</Option>
                                    ))}
                                </Select>
                            </Col>
                            <Col xs={24} md={8}>
                                <Select
                                    showSearch
                                    allowClear
                                    placeholder="Фильтр по предмету"
                                    style={{ width: '100%' }}
                                    value={selectedSubjectId}
                                    onChange={handleSubjectFilterChange}
                                    filterOption={(input, option) =>
                                        (option?.children ?? '').toLowerCase().includes(input.toLowerCase())
                                    }
                                    disabled={loadingFilters || !filterSubjects.length}
                                >
                                    {filterSubjects.map(subject => (
                                        <Option key={subject.id} value={subject.id}>{subject.name}</Option>
                                    ))}
                                </Select>
                            </Col>
                        </Row>
                    </Space>
                </Spin>
            )}

            <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleAdd}
                style={{ marginBottom: 16 }}
            >
                Добавить отзыв
            </Button>

            <Spin spinning={loading}>
                {filteredReviews.length > 0 ? (
                    <List
                        grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 2, xl: 3, xxl: 3 }}
                        dataSource={filteredReviews}
                        renderItem={(review) => (
                            <List.Item key={review.id}>
                                <Card
                                    hoverable
                                    title={
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                            <TeamOutlined />
                                            {mode !== 'teacher' && review.teacher?.id ? (
                                                <Link to={`/teachers/${review.teacher.id}/reviews`}>
                                                    {getTeacherFullName(review.teacher)}
                                                </Link>
                                            ) : (
                                                getTeacherFullName(review.teacher)
                                            )}
                                        </div>
                                    }
                                    actions={[
                                        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(review)}>Редактировать</Button>,
                                        <Popconfirm
                                            title="Удалить отзыв?"
                                            description="Вы уверены, что хотите удалить этот отзыв?"
                                            onConfirm={() => handleDelete(review.id)}
                                            okText="Да, удалить"
                                            cancelText="Отмена"
                                        >
                                            <Button type="link" danger icon={<DeleteOutlined />}>Удалить</Button>
                                        </Popconfirm>,
                                    ]}
                                >
                                    <div style={{ marginBottom: '10px' }}>
                                        <Tag icon={<BookOutlined />} color="blue">{review.subjectName || 'N/A'}</Tag>
                                        {mode !== 'user' && review.authorId ? (
                                            <Tag icon={<UserOutlined />} color="purple">
                                                <Link to={`/users/${review.authorId}/reviews`} style={{ color: 'inherit' }}>
                                                    {review.author || 'N/A'}
                                                </Link>
                                            </Tag>
                                        ) : (
                                            <Tag icon={<UserOutlined />} color="purple">{review.author || 'N/A'}</Tag>
                                        )}
                                        <Tag icon={<CalendarOutlined />} color="default">
                                            {review.date ? dayjs(review.date).format('DD.MM.YYYY') : 'N/A'}
                                        </Tag>
                                    </div>
                                    <Rate disabled allowHalf value={review.grade / 2} style={{ marginBottom: '10px' }} />
                                    <Text strong> ({review.grade}/10)</Text>
                                    <Paragraph ellipsis={{ rows: 3, expandable: true, symbol: 'ещё' }} style={{ marginTop: '10px' }}>
                                        {review.comment || <Text type="secondary">Комментарий отсутствует</Text>}
                                    </Paragraph>

                                </Card>
                            </List.Item>
                        )}
                    />
                ) : (
                    !loading && <Empty description={
                        (selectedAuthorId || selectedTeacherId || selectedSubjectId)
                            ? "Отзывы по заданным фильтрам не найдены"
                            : (mode === 'all' ? "Отзывов пока нет" : "Для данного объекта отзывов не найдено")
                    } />
                )}
            </Spin>
            <ReviewForm
                visible={isModalVisible}
                onCreate={handleCreate}
                onUpdate={handleUpdate}
                onCancel={handleCancel}
                editingReview={editingReview}
            />
        </div>
    );
};

export default ReviewList;