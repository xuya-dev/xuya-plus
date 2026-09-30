<script setup lang="ts">
import CrudTable from '@/components/crud-table/index.vue';
import { roleService } from '@/service/api/system';

const config = {
  title: '角色管理',
  api: roleService,
  defaultForm: { status: 0, roleSort: 0 },
  columns: [
    { key: 'id', title: 'ID', width: 60, hidden: true },
    { key: 'roleName', title: '角色名称', search: true, form: 'input' },
    { key: 'roleKey', title: '权限字符', form: 'input' },
    { key: 'roleSort', title: '排序', form: 'number' },
    { key: 'status', title: '状态', type: 'tag-success' as const, form: 'select' as const, formOptions: [{ label: '正常', value: 0 }, { label: '停用', value: 1 }] },
    { key: 'createTime', title: '创建时间', type: 'datetime' as const },
  ],
};
</script>

<template>
  <CrudTable :config="config" />
</template>
