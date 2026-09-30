<script setup lang="ts">
import CrudTable from '@/components/crud-table/index.vue';
import { configService } from '@/service/api/system';

const config = {
  title: '参数设置',
  api: configService,
  defaultForm: { configType: 'N' },
  columns: [
    { key: 'id', title: 'ID', width: 60, hidden: true },
    { key: 'configName', title: '参数名称', search: true, form: 'input' },
    { key: 'configKey', title: '参数键名', search: true, form: 'input' },
    { key: 'configValue', title: '参数键值', form: 'input' },
    { key: 'configType', title: '内置', form: 'select' as const, formOptions: [{ label: '是', value: 'Y' }, { label: '否', value: 'N' }] },
    { key: 'remark', title: '备注', form: 'textarea' },
  ],
};
</script>

<template>
  <CrudTable :config="config" />
</template>
