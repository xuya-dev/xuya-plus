import { request } from '../request';

// ===== 通用类型 =====
interface PageResult<T> {
  code: number;
  msg: string;
  data: { records: T[]; total: number; size: number; current: number };
}

function page<T>(url: string, params: Record<string, any>) {
  return request<PageResult<T>['data']>({ url, method: 'get', params });
}

function post<T = void>(url: string, data?: any) {
  return request<T>({ url, method: 'post', data });
}

function put<T = void>(url: string, data?: any) {
  return request<T>({ url, method: 'put', data });
}

function del(url: string) {
  return request<void>({ url, method: 'delete' });
}

// ===== 用户管理 =====
export interface SysUser {
  id: number;
  username: string;
  nickname: string;
  deptId: number;
  deptName?: string;
  phone: string;
  email: string;
  status: number;
  createTime: string;
}

export const userService = {
  page: (params: Record<string, any>) => page<SysUser>('/sys-user/page', { current: 1, size: 10, ...params }),
  getById: (id: number) => request<SysUser>({ url: `/sys-user/${id}` }),
  create: (data: Record<string, any>) => post('/sys-user', data),
  update: (data: Record<string, any>) => put('/sys-user', data),
  remove: (ids: string) => del(`/sys-user/${ids}`),
};

// ===== 角色管理 =====
export interface SysRole {
  id: number;
  roleName: string;
  roleKey: string;
  roleSort: number;
  status: number;
}

export const roleService = {
  page: (params: Record<string, any>) => page<SysRole>('/sys-role/page', { current: 1, size: 10, ...params }),
  list: () => request<SysRole[]>({ url: '/sys-role/list' }),
  getById: (id: number) => request<SysRole>({ url: `/sys-role/${id}` }),
  create: (data: Record<string, any>) => post('/sys-role', data),
  update: (data: Record<string, any>) => put('/sys-role', data),
  remove: (ids: string) => del(`/sys-role/${ids}`),
};

// ===== 部门管理 =====
export interface SysDept {
  id: number;
  parentId: number;
  deptName: string;
  orderNum: number;
  status: number;
  children?: SysDept[];
}

export const deptService = {
  tree: () => request<SysDept[]>({ url: '/sys-dept/tree' }),
  list: () => request<SysDept[]>({ url: '/sys-dept/list' }),
  create: (data: Record<string, any>) => post('/sys-dept', data),
  update: (data: Record<string, any>) => put('/sys-dept', data),
  remove: (ids: string) => del(`/sys-dept/${ids}`),
};

// ===== 岗位管理 =====
export interface SysPost {
  id: number;
  postCode: string;
  postName: string;
  postSort: number;
  status: number;
}

export const postService = {
  page: (params: Record<string, any>) => page<SysPost>('/sys-post/page', { current: 1, size: 10, ...params }),
  list: () => request<SysPost[]>({ url: '/sys-post/list' }),
  create: (data: Record<string, any>) => post('/sys-post', data),
  update: (data: Record<string, any>) => put('/sys-post', data),
  remove: (ids: string) => del(`/sys-post/${ids}`),
};

// ===== 字典管理 =====
export interface SysDictType {
  id: number;
  dictName: string;
  dictType: string;
  status: number;
}

export interface SysDictData {
  id: number;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  dictSort: number;
  status: number;
}

export const dictTypeService = {
  page: (params: Record<string, any>) => page<SysDictType>('/sys-dict-type/page', { current: 1, size: 10, ...params }),
  create: (data: Record<string, any>) => post('/sys-dict-type', data),
  update: (data: Record<string, any>) => put('/sys-dict-type', data),
  remove: (ids: string) => del(`/sys-dict-type/${ids}`),
};

export const dictDataService = {
  page: (params: Record<string, any>) => page<SysDictData>('/sys-dict-data/page', { current: 1, size: 10, ...params }),
  getByType: (dictType: string) => request<SysDictData[]>({ url: `/sys-dict-data/type/${dictType}` }),
  create: (data: Record<string, any>) => post('/sys-dict-data', data),
  update: (data: Record<string, any>) => put('/sys-dict-data', data),
  remove: (ids: string) => del(`/sys-dict-data/${ids}`),
};

// ===== 参数配置 =====
export interface SysConfig {
  id: number;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
}

export const configService = {
  page: (params: Record<string, any>) => page<SysConfig>('/sys-config/page', { current: 1, size: 10, ...params }),
  getByKey: (key: string) => request<string>({ url: `/sys-config/key/${key}` }),
  create: (data: Record<string, any>) => post('/sys-config', data),
  update: (data: Record<string, any>) => put('/sys-config', data),
  remove: (ids: string) => del(`/sys-config/${ids}`),
};

// ===== 通知公告 =====
export interface SysNotice {
  id: number;
  noticeTitle: string;
  noticeType: number;
  noticeContent: string;
  status: number;
}

export const noticeService = {
  page: (params: Record<string, any>) => page<SysNotice>('/sys-notice/page', { current: 1, size: 10, ...params }),
  create: (data: Record<string, any>) => post('/sys-notice', data),
  update: (data: Record<string, any>) => put('/sys-notice', data),
  remove: (ids: string) => del(`/sys-notice/${ids}`),
};

// ===== 文件管理 =====
export interface SysOss {
  id: number;
  fileName: string;
  originalName: string;
  url: string;
  service: string;
  fileSize: number;
}

export const ossService = {
  page: (params: Record<string, any>) => page<SysOss>('/system/oss/page', { current: 1, size: 10, ...params }),
  upload: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request<SysOss>({ url: '/system/oss/upload', method: 'post', data: formData });
  },
  remove: (ids: string) => del(`/system/oss/${ids}`),
};

// ===== 定时任务 =====
export interface SysJob {
  id: number;
  jobName: string;
  invokeTarget: string;
  cronExpression: string;
  status: number;
}

export const jobService = {
  page: (params: Record<string, any>) => page<SysJob>('/sys-job/page', { current: 1, size: 10, ...params }),
  create: (data: Record<string, any>) => post('/sys-job', data),
  update: (data: Record<string, any>) => put('/sys-job', data),
  remove: (ids: string) => del(`/sys-job/${ids}`),
  changeStatus: (id: number, status: number) => put(`/sys-job/changeStatus/${id}/${status}`),
  runOnce: (id: number) => post(`/sys-job/run/${id}`),
};
