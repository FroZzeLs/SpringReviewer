import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { List, Button, Typography, Spin, Popconfirm, message, Tag, Input, Space } from 'antd';
import { BookOutlined, PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import * as api from '../../api';
import SubjectForm from './SubjectForm';

const { Title, Text } = Typography;
const { Search } = Input;

const SubjectList = () => {
    const [allSubjects, setAllSubjects] = useState([]);
    const [filteredSubjects, setFilteredSubjects] = useState([]);
    const [loading, setLoading] = useState(false);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingSubject, setEditingSubject] = useState(null);
    const [searchText, setSearchText] = useState('');
    const [messageApi, contextHolder] = message.useMessage();

    const fetchSubjects = useCallback(async () => {
        setLoading(true);
        try {
            const response = await api.getSubjects();
            setAllSubjects(response.data);
            setFilteredSubjects(response.data);
            setSearchText('');
        } catch (error) {
            api.handleApiError(error, messageApi);
        } finally {
            setLoading(false);
        }
    }, [messageApi]);

    useEffect(() => {
        fetchSubjects();
    }, [fetchSubjects]);

    useEffect(() => {
        const lowercasedFilter = searchText.toLowerCase();
        const filteredData = allSubjects.filter((item) =>
            item.name.toLowerCase().includes(lowercasedFilter)
        );
        setFilteredSubjects(filteredData);
    }, [searchText, allSubjects]);

    const handleSearch = (value) => {
        setSearchText(value);
    };

    const handleAdd = () => {
        setEditingSubject(null);
        setIsModalVisible(true);
    };

    const handleEdit = (subject) => {
        setEditingSubject(subject);
        setIsModalVisible(true);
    };

    const handleDelete = async (id) => {
        try {
            await api.deleteSubject(id);
            messageApi.success('Предмет успешно удален');
            fetchSubjects();
        } catch (error) {
            api.handleApiError(error, messageApi);
        }
    };

    const handleCreate = async (values) => {
        try {
            await api.createSubject(values);
            messageApi.success('Предмет успешно создан');
            setIsModalVisible(false);
            fetchSubjects();
        } catch (error) {
            api.handleApiError(error, messageApi);
        }
    };

    const handleUpdate = async (id, values) => {
        try {
            await api.updateSubject(id, values);
            messageApi.success('Предмет успешно обновлен');
            setIsModalVisible(false);
            setEditingSubject(null);
            fetchSubjects();
        } catch (error) {
            api.handleApiError(error, messageApi);
        }
    };

    const handleCancel = () => {
        setIsModalVisible(false);
        setEditingSubject(null);
    };

    return (
        <div className="list-container">
            {contextHolder}
            <Title level={4} className="section-title">Предметы</Title>
            <Space direction="vertical" style={{ width: '100%', marginBottom: 16 }}>
                <Search
                    placeholder="Поиск по названию предмета"
                    allowClear
                    enterButton="Найти"
                    value={searchText}
                    onChange={(e) => setSearchText(e.target.value)}
                    onSearch={handleSearch}
                />
                <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleAdd}
                >
                    Добавить предмет
                </Button>
            </Space>
            <Spin spinning={loading}>
                <List
                    itemLayout="horizontal"
                    dataSource={filteredSubjects}
                    renderItem={(subject) => (
                        <List.Item
                            className="list-item-margin"
                            actions={[
                                <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(subject)}>Редактировать</Button>,
                                <Popconfirm
                                    title="Удалить предмет?"
                                    description={`Вы уверены, что хотите удалить ${subject.name}?`}
                                    onConfirm={() => handleDelete(subject.id)}
                                    okText="Да, удалить"
                                    cancelText="Отмена"
                                >
                                    <Button type="link" danger icon={<DeleteOutlined />}>Удалить</Button>
                                </Popconfirm>,
                            ]}
                        >
                            <List.Item.Meta
                                avatar={<BookOutlined style={{ fontSize: '20px', color: '#1677ff' }}/>}
                                title={<Text strong>{subject.name}</Text>}
                                description={
                                    subject.teacherNames && subject.teacherNames.length > 0
                                        ? <>Преподаватели: {subject.teacherNames.map(name => <Tag key={name} color="blue">{name}</Tag>)}</>
                                        : 'Преподаватели не назначены'
                                }
                            />
                        </List.Item>
                    )}
                    locale={{ emptyText: searchText ? 'Предметы не найдены' : 'Нет предметов' }}
                />
            </Spin>
            <SubjectForm
                visible={isModalVisible}
                onCreate={handleCreate}
                onUpdate={handleUpdate}
                onCancel={handleCancel}
                editingSubject={editingSubject}
            />
        </div>
    );
};

export default SubjectList;