'use strict'   
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_API: "'api'" // 加入这一句
/*  BASE_API: '"http://localhost:48882"' */
})


//环境配置