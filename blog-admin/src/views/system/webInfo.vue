<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>网站全局配置</span>
          <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px" v-loading="loading">
        <el-tabs v-model="activeTab">

          <el-tab-pane label="基础信息" name="basic">
            <el-form-item label="网站名称">
              <el-input v-model="form.siteName" placeholder="例如：Myo Nexus" />
            </el-form-item>
            <el-form-item label="网站描述(SEO)">
              <el-input type="textarea" v-model="form.siteDesc" rows="3" />
            </el-form-item>
            <el-form-item label="关键字(SEO)">
              <el-input v-model="form.keywords" placeholder="多个关键字用英文逗号分隔" />
            </el-form-item>
            <el-form-item label="站长名称">
              <el-input v-model="form.author" />
            </el-form-item>
            <el-form-item label="全局公告">
              <el-input type="textarea" v-model="form.notice" rows="3" placeholder="支持HTML格式" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="功能开关" name="switch">
            <el-form-item label="网站状态">
              <el-switch v-model="form.siteStatus" :active-value="1" :inactive-value="0" active-text="正常运行" inactive-text="维护中" />
            </el-form-item>
            <el-form-item label="开放注册">
              <el-switch v-model="form.allowRegister" :active-value="1" :inactive-value="0" active-text="允许缔结新约" inactive-text="关闭注册" />
              <span style="margin-left: 15px; color: #999; font-size: 12px;">关闭后前台将隐藏注册表单并提示暂不开放</span>
            </el-form-item>
            <el-form-item label="全局评论">
              <el-switch v-model="form.allowComment" :active-value="1" :inactive-value="0" active-text="允许评论" inactive-text="全站禁言" />
            </el-form-item>
            <el-form-item label="访客统计">
              <el-switch v-model="form.showVisitorCount" :active-value="1" :inactive-value="0" active-text="底部显示" inactive-text="隐藏" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="社交信息" name="social">
            <el-form-item label="GitHub 主页">
              <el-input v-model="form.githubUrl" />
            </el-form-item>
            <el-form-item label="Gitee 主页">
              <el-input v-model="form.giteeUrl" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item label="联系 QQ">
              <el-input v-model="form.qqNumber" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="备案版权" name="legal">
            <el-form-item label="ICP 备案号">
              <el-input v-model="form.icpRecord" placeholder="例如：湘ICP备xxxxxxxx号" />
            </el-form-item>
            <el-form-item label="公安网备号">
              <el-input v-model="form.policeRecord" />
            </el-form-item>
            <el-form-item label="版权年份">
              <el-input v-model="form.copyrightYear" placeholder="例如：2023-2026" />
            </el-form-item>
          </el-tab-pane>

        </el-tabs>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWebInfo, updateWebInfo } from '../../api/webInfo'

const activeTab = ref('basic')
const loading = ref(false)
const saving = ref(false)

const form = ref({
  id: 1,
  siteName: '',
  siteDesc: '',
  keywords: '',
  author: '',
  notice: '',
  siteStatus: 1,
  allowRegister: 1,
  allowComment: 1,
  showVisitorCount: 1,
  githubUrl: '',
  giteeUrl: '',
  email: '',
  qqNumber: '',
  icpRecord: '',
  policeRecord: '',
  copyrightYear: ''
})

const fetchWebInfo = async () => {
  loading.value = true
  try {
    const res: any = await getWebInfo()

    // 兼容 Axios 拦截器是否解包的不同情况，获取真实的 JSON 响应体
    const responseBody = res.code !== undefined ? res : res.data

    if (responseBody && (responseBody.code === 200 || responseBody.success)) {
      // 采用展开语法重新赋值，确保必定触发 Vue3 的响应式视图更新
      form.value = { ...form.value, ...responseBody.data }
    }
  } catch (error) {
    ElMessage.error('获取网站配置失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const res: any = await updateWebInfo(form.value)

    const responseBody = res.code !== undefined ? res : res.data

    if (responseBody && (responseBody.code === 200 || responseBody.success)) {
      ElMessage.success('网站配置保存成功！')
    } else {
      ElMessage.error(responseBody.msg || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存出现异常')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchWebInfo()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>