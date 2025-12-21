<template>

  <el-card :body-style="{ padding: '8px 18px' }">
    <div slot="header" class="me-category-header">
      <span>{{cardHeader}}</span>
    </div>
	
  </el-card>
</template>
<script>

  export default {
    name: "CardTechnology",
    props: {
      cardHeader: {
        type: String,
        required: true
      },
      archives: {
        type: Array,
        required: true
      }
    },
	mounted() {
		this.showIn();
	},
    methods: {
      showIn(){

	  }

    }
  }
</script>

<style scoped>

  .me-category-header {
    font-weight: 600;
  }

  .me-category-list {
    list-style-type: none;
  }

  .me-category-item {
    display: inline-block;
    width: 40%;
    padding: 4px;
    font-size: 14px;
    color: #000000;
  }

  .me-category-item a:hover {
    text-decoration: underline;
  }
  


</style>
