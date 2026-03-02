<template>
  <div class="asset-image-wall">
    <div class="wall-toolbar">
      <span class="wall-count">图片 {{ items.length }}/{{ max }}</span>
      <span v-if="tip" class="wall-tip">{{ tip }}</span>
    </div>

    <div class="wall-grid">
      <div
        v-for="(item, index) in items"
        :key="item.uid || `${item.url}-${index}`"
        class="wall-card"
        :class="{
          'is-uploading': item.status === 'uploading',
          'is-error': item.status === 'error',
          'is-dragging': dragIndex === index,
          'is-drop-target': dragOverIndex === index
        }"
        :draggable="!disabled && !uploading && item.status !== 'uploading'"
        @dragstart="handleDragStart(index, $event)"
        @dragenter.prevent="handleDragEnter(index)"
        @dragover.prevent
        @drop.prevent="handleDrop(index)"
        @dragend="handleDragEnd"
      >
        <button
          type="button"
          class="delete-btn"
          :disabled="item.status === 'uploading'"
          title="删除图片"
          @click.stop="$emit('remove', item, index)"
        >
          <el-icon><Close /></el-icon>
        </button>

        <el-image
          class="wall-image"
          :src="item.url"
          fit="cover"
          :preview-src-list="previewList"
          :initial-index="index"
          preview-teleported
        />

        <div class="wall-mask"></div>

        <div v-if="item.status === 'uploading'" class="wall-status">上传中...</div>
        <div v-else-if="item.status === 'error'" class="wall-status error">
          {{ item.errorMessage || '上传失败' }}
        </div>
      </div>

      <button
        v-if="canAdd"
        type="button"
        class="upload-card"
        :disabled="disabled || uploading"
        @click="openFilePicker"
      >
        <el-icon class="upload-icon"><Plus /></el-icon>
        <span class="upload-text">{{ uploading ? '上传中' : '添加图片' }}</span>
      </button>
    </div>

    <input
      ref="fileInputRef"
      class="hidden-input"
      type="file"
      accept="image/*"
      multiple
      @change="handleFileInput"
    >
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Plus, Close } from '@element-plus/icons-vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
  max: {
    type: Number,
    default: 5
  },
  disabled: {
    type: Boolean,
    default: false
  },
  uploading: {
    type: Boolean,
    default: false
  },
  tip: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['select-files', 'remove', 'reorder'])

const fileInputRef = ref(null)
const dragIndex = ref(-1)
const dragOverIndex = ref(-1)

const previewList = computed(() => props.items.map(item => item.url).filter(Boolean))
const canAdd = computed(() => props.items.length < props.max)

const openFilePicker = () => {
  if (props.disabled || props.uploading || !canAdd.value) return
  fileInputRef.value?.click()
}

const handleFileInput = (event) => {
  const files = Array.from(event.target.files || [])
  if (files.length) {
    emit('select-files', files)
  }
  event.target.value = ''
}

const handleDragStart = (index, event) => {
  if (props.disabled || props.uploading) return
  dragIndex.value = index
  dragOverIndex.value = index
  if (event?.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    try {
      event.dataTransfer.setData('text/plain', String(index))
    } catch {
      // ignore
    }
  }
}

const handleDragEnter = (index) => {
  if (dragIndex.value < 0) return
  dragOverIndex.value = index
}

const handleDrop = (index) => {
  if (dragIndex.value < 0) {
    handleDragEnd()
    return
  }
  const fromIndex = dragIndex.value
  const toIndex = index
  if (fromIndex !== toIndex) {
    emit('reorder', { fromIndex, toIndex })
  }
  handleDragEnd()
}

const handleDragEnd = () => {
  dragIndex.value = -1
  dragOverIndex.value = -1
}
</script>

<style scoped>
.asset-image-wall {
  width: 100%;
}

.wall-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-bottom: 12px;
  color: #606266;
  font-size: 12px;
}

.wall-count {
  color: #303133;
  font-weight: 600;
}

.wall-tip {
  color: #909399;
}

.wall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(132px, 1fr));
  gap: 12px;
}

.wall-card {
  position: relative;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  cursor: grab;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.wall-card.is-dragging {
  opacity: 0.7;
  transform: scale(0.98);
  cursor: grabbing;
}

.wall-card.is-drop-target {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
}

.wall-image {
  display: block;
  width: 100%;
  height: 132px;
  background: #f5f7fa;
}

.delete-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease, background 0.2s ease;
}

.delete-btn:hover {
  background: rgba(245, 108, 108, 0.9);
}

.delete-btn:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.wall-card:hover .delete-btn {
  opacity: 1;
}

.wall-mask {
  position: absolute;
  inset: auto 0 0 0;
  padding: 8px 8px 6px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.wall-card:hover .wall-mask {
  opacity: 1;
}

.wall-status {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.85);
  color: #409eff;
  font-size: 12px;
  font-weight: 500;
}

.wall-status.error {
  color: #f56c6c;
}

.upload-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 132px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: #fafafa;
  color: #909399;
  cursor: pointer;
  transition: all 0.2s ease;
}

.upload-card:hover {
  border-color: #409eff;
  color: #409eff;
  background: #f5faff;
}

.upload-card:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.upload-icon {
  font-size: 22px;
}

.upload-text {
  font-size: 12px;
}

.hidden-input {
  display: none;
}
</style>
