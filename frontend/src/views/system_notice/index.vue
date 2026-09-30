<script setup lang="ts">
import CrudTable from '@/components/crud-table/index.vue';
import { noticeService } from '@/service/api/system';

const config = {
  title: '通知公告',
  api: noticeService,
  defaultForm: { status: 0, noticeType: 1 },
  columns: [
    { key: 'id', title: 'ID', width: 60, hidden: true },
    { key: 'noticeTitle', title: '公告标题', search: true, form: 'input' },
    { key: 'noticeType', title: '类型', type: 'tag-success' as const, form: 'select' as const, formOptions: [{ label: '通知', value: 1 }, { label: '公告', value: 2 }] },
    { key: 'status', title: '状态', type: 'tag-success' as const, form: 'select' as const, formOptions: [{ label: '正常', value: 0 }, { label: '关闭', value: 1 }] },
    { key: 'createTime', title: '创建时间', type: 'datetime' as const },
  ],
};
</script>

<template>
  <CrudTable :config="config" />
</template>
