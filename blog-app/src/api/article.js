import request from '@/request'

export function getMAC() {
  return request({
    url: `/articles/queryMAC`,
    method: 'post',
  })
}

export function getArticles(query, page,token) {
  return request({
	headers: {'Authorization': token},
    url: '/articles',
    method: 'post',
    data: {
      page: page.pageNumber,
      pageSize: page.pageSize,
      name: page.name,
      sort: page.sort,
      year: page.year,
      month: page.month,
      tagId: query.tagId,
      categoryId: query.categoryId
    }
  })
}

export function getHotArtices() {
  return request({
    url: '/articles/hot',
    method: 'post'
  })
}

export function getNewArtices() {
  return request({
    url: '/articles/new',
    method: 'post'
  })
}

export function viewArticle(id,token) {
  return request({
	headers: {'Authorization': token},
    url: `/articles/view/${id}`,
    method: 'post'
  })
}

export function getArticlesByCategory(id) {
  return request({
    url: `/articles/category/${id}`,
    method: 'post'
  })
}

export function getArticlesByTag(id) {
  return request({
    url: `/articles/tag/${id}`,
    method: 'post'
  })
}


export function publishArticle(article,token) {
  return request({
    headers: {'Authorization': token},
    url: '/articles/publish',
    method: 'post',
    data: article
  })
}

export function listArchives() {
  return request({
    url: '/articles/listArchives',
    method: 'post'
  })
}

export function getArticleById(id) {
  return request({
    url: `/articles/${id}`,
    method: 'post'
  })
}

export function getListArticleCount(token) {
  return request({
	headers: {'Authorization': token},
    url: `/articles/getListArticleCount`,
    method: 'post'
  })
}
