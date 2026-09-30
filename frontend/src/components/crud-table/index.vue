<script setup lang="ts">
import { ref, computed, onMounted, h, watch } from 'vue';
import { NButton, NCard, NDataTable, NInput, NModal, NForm, NFormItem, NPopconfirm, NSelect, NSpace, useMessage } from 'naive-ui';

export interface CrudColumn {
  key: string;
  title: string;
  width?: number;
  type?: 'text' | 'tag' | 'tag-success' | 'tag-error' | 'datetime';
  search?: boolean;
  form?: 'input' | 'number' | 'select' | 'textarea';
  formOptions?: { label: string; value: number | string }[];
  hidden?: boolean;
}

export interface CrudConfig<T = any> {
  title: string;
  api: {
    page: (params: Record<string, any>) => Promise<{ data: { records: T[]; total: number } }>;
    create: (data: Record<string, any>) => Promise<any>;
    update: (data: Record<string, any>) => Promise<any>;
    remove: (ids: string) => Promise<any>;
  };
  columns: CrudColumn[];
  defaultForm?: Record<string, any>;
}

const props = defineProps<{ config: CrudConfig }>();
const message = useMessage();

const loading = ref(false);
const data = ref<any[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const searchParams = ref<Record<string, string>>({});
const showModal = ref(false);
const editingId = ref<number | null>(null);
const formModel = ref<Record<string, any>>({});

const searchFields = computed(() => props.config.columns.filter(c => c.search));
const formFields = computed(() => props.config.columns.filter(c => !c.hidden && c.key !== 'id'));

function buildForm() {
  const f: Record<string, any> = { ...(props.config.defaultForm || {}) };
  for (const c of props.config.columns) {
    if (!c.hidden && c.key !== 'children' && f[c.key] === undefined) {
      f[c.key] = c.form === 'number' ? 0 : '';
    }
  }
  return f;
}

async function fetchData() {
  loading.value = true;
  try {
    const params: Record<string, any> = { current: page.value, size: pageSize.value, ...searchParams.value };
    const res = await props.config.api.page(params);
    data.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch { /* ignore */ }
  loading.value = false;
}

function handleSearch() {
  page.value = 1;
  fetchData();
}

function openAdd() {
  editingId.value = null;
  formModel.value = buildForm();
  showModal.value = true;
}

function openEdit(row: any) {
  editingId.value = row.id;
  formModel.value = { ...row };
  showModal.value = true;
}

async function handleSubmit() {
  try {
    if (editingId.value) {
      await props.config.api.update({ ...formModel.value, id: editingId.value });
      message.success('修改成功');
    } else {
      await props.config.api.create(formModel.value);
      message.success('新增成功');
    }
    showModal.value = false;
    fetchData();
  } catch { message.error('操作失败'); }
}

async function handleDelete(id: number) {
  await props.config.api.remove(String(id));
  message.success('删除成功');
  fetchData();
}

function renderCell(row: any, col: CrudColumn) {
  const val = row[col.key];
  if (col.type === 'tag-success') return h('span', { style: 'color:#18a058' }, val ?? '');
  if (col.type === 'tag-error') return h('span', { style: 'color:#d03050' }, val ?? '');
  if (col.type === 'datetime' && val) return val?.substring(0, 19);
  return val ?? '';
}

const tableColumns = computed(() =>
  props.config.columns
    .filter(c => !c.hidden)
    .map(c => ({
      key: c.key,
      title: c.title,
      width: c.width,
      render: (row: any) => renderCell(row, c),
    }))
    .concat({
      key: 'actions',
      title: '操作',
      width: 160,
      render: (row: any) => {
        return [
          h(NButton, { size: 'small', onClick: () => openEdit(row), style: 'margin-right:8px' }, () => '编辑'),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
            default: () => '确认删除？',
          }),
        ];
      },
    })
);

function onSearchChange(key: string, value: string) {
  searchParams.value[key] = value;
}

onMounted(fetchData);

defineExpose({ fetchData });
</script>

<template>
  <NCard :title="config.title">
    <NSpace v-if="searchFields.length" style="margin-bottom: 12px" align="center">
      <template v-for="sf in searchFields" :key="sf.key">
        <NInput
          :placeholder="sf.title"
          style="width: 180px"
          clearable
          @update:value="v => onSearchChange(sf.key, v)"
          @keyup.enter="handleSearch"
        />
      </template>
      <NButton type="primary" @click="handleSearch">搜索</NButton>
    </NSpace>
    <NSpace v-if="!editingId" style="margin-bottom: 12px" justify="end">
      <NButton type="primary" @click="openAdd">新增</NButton>
    </NSpace>
    <NDataTable
      :columns="tableColumns"
      :data="data"
      :loading="loading"
      remote
      :pagination="{ page, pageSize, itemCount: total, onChange: (p: number) => { page = p; fetchData(); } }"
    />
    <NModal v-model:show="showModal" preset="card" :title="editingId ? '编辑' : '新增'" style="width: 520px">
      <NForm :model="formModel" label-placement="left" label-width="90">
        <template v-for="col in formFields" :key="col.key">
          <NFormItem :label="col.title">
            <NSelect
              v-if="col.form === 'select' && col.formOptions"
              v-model:value="formModel[col.key]"
              :options="col.formOptions"
            />
            <NInput
              v-else-if="col.form === 'textarea'"
              v-model:value="formModel[col.key]"
              type="textarea"
              :rows="3"
            />
            <NInputNumber
              v-else-if="col.form === 'number'"
              v-model:value="formModel[col.key]"
              style="width: 100%"
            />
            <NInput v-else v-model:value="formModel[col.key]" />
          </NFormItem>
        </template>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </NCard>
</template>
