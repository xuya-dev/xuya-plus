<script setup lang="ts">
import CrudTable from '@/components/crud-table/index.vue';
import { postService } from '@/service/api/system';

const config = {
  title: '岗位管理',
  api: postService,
  defaultForm: { status: 0, postSort: 0 },
  columns: [
    { key: 'id', title: 'ID', width: 60, hidden: true },
    { key: 'postCode', title: '岗位编码', search: true, form: 'input' },
    { key: 'postName', title: '岗位名称', search: true, form: 'input' },
    { key: 'postSort', title: '排序', form: 'number' },
    { key: 'status', title: '状态', type: 'tag-success' as const, form: 'select' as const, formOptions: [{ label: '正常', value: 0 }, { label: '停用', value: 1 }] },
    { key: 'createTime', title: '创建时间', type: 'datetime' as const },
  ],
};
</script>

<template>
  <CrudTable :config="config" />
</template>
