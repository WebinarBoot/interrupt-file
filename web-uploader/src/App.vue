<template>
  <div>
    <a-button @click="uploadFile"> 资源上传 </a-button>
    <a-drawer
      title="资源上传"
      placement="right"
      width="1000"
      :closable="false"
      :visible="visible"
      @close="onClose"
    >
      <upload></upload>
    </a-drawer>
  </div>
</template>

<script>
import Upload from "./components/Upload/Index";
import axios from "axios";
import storage from "store";
export default {
  name: 'app',
  data () {
    return {
      visible: true,
    };
  },
  components: {
    Upload,
  },
  methods: {
    uploadFile() {
      this.visible = true;
    },
    onClose() {
      this.visible = false;
    }
  },
  created() {
    axios
      .get(
        "http://127.0.0.1:8888/upload/hello",
        {
        }
      )
      .then((res) => {
      })
  }
}
</script>
