<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公文动态盖章测试台</span>
        </div>
      </template>

      <el-upload
          class="upload-demo"
          drag
          action=""
          :http-request="handleUpload"
          :show-file-list="false"
          accept=".docx"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将 Word 公文拖到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传 .docx 格式文件，并在文档任意位置写上“盖章处”三个字作为测试锚点
          </div>
        </template>
      </el-upload>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

// 这里使用相对路径，往上退两层跳到 src 目录，然后再进入 utils
import request from '../../utils/request'

const handleUpload = async (options: any) => {
  const file = options.file
  const formData = new FormData()
  formData.append('file', file)

  try {
    ElMessage.info('文档处理中，正在寻找盖章位置...')

    const res = await request({
      url: '/admin/word/stamp',
      method: 'post',
      data: formData,
      responseType: 'blob'
    })

    const blob = new Blob([res.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '已盖章_回传公文.docx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)

    ElMessage.success('盖章成功！请查看下载的文件')
  } catch (error) {
    console.error(error)
    ElMessage.error('盖章失败，请检查后端日志')
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.upload-demo {
  text-align: center;
  margin-top: 50px;
  margin-bottom: 50px;
}
</style>

