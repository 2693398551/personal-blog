import Vuex from 'vuex'
import Vue from 'vue'
import {getToken, setToken, removeToken} from '@/request/token'
import {login, getUserInfo, logout, register} from '@/api/login'
import { getWebInfo } from '@/api/webInfo'
Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    webInfo: {},
    id: '',
    account: '',
    name: '',
    avatar: '',
    token: getToken(),
    email: '',
    mobilePhoneNumber: '',
    sex: 0,
    // 新增
    bio: '',
    birthday: null,
    website: '',
    source: 1,
    // 游客数据初始值
    guest: {
      uuid: '',
      nickname: '旅人',
      email: '',
      website: '',
      avatar: ''
    }
  },
  mutations: {
    SET_WEB_INFO: (state, webInfo) => {
      state.webInfo = webInfo
    },
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_ACCOUNT: (state, account) => {
      state.account = account
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ID: (state, id) => {
      state.id = id
    },
    SET_EMAIL: (state, email) => {
      state.email = email
    },
    SET_MOBILE_PHONE_NUMBER: (state, mobilePhoneNumber) => {
      state.mobilePhoneNumber = mobilePhoneNumber
    },
    SET_SEX: (state, sex) => {
      state.sex = sex
    },
    // 新增
    SET_BIO: (state, bio) => {
      state.bio = bio || ''
    },
    SET_BIRTHDAY: (state, birthday) => {
      state.birthday = birthday || null
    },
    SET_WEBSITE: (state, website) => {
      state.website = website || ''
    },
    SET_SOURCE: (state, source) => {
      state.source = source || 1
    },

    // 游客逻辑（不变）
    INIT_GUEST(state) {
      const stored = localStorage.getItem('LUNA_GUEST_INFO')
      if (stored) {
        try {
          state.guest = JSON.parse(stored)
          console.log('欢迎回来，老朋友：', state.guest.nickname)
        } catch (e) {
          localStorage.removeItem('LUNA_GUEST_INFO')
        }
      }
      if (!state.guest.uuid) {
        console.log('是新朋友，正在生成身份...')
        const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
          var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8)
          return v.toString(16)
        })
        const suffix = uuid.substring(0, 4).toUpperCase()
        const newGuest = { uuid, nickname: `旅人${suffix}`, email: '', website: '', avatar: '' }
        state.guest = newGuest
        localStorage.setItem('LUNA_GUEST_INFO', JSON.stringify(newGuest))
      }
    },
    UPDATE_GUEST(state, payload) {
      state.guest = { ...state.guest, ...payload }
      localStorage.setItem('LUNA_GUEST_INFO', JSON.stringify(state.guest))
    }
  },
  actions: {
    // 获取网站信息
    getWebInfo({commit}) {
      return new Promise((resolve, reject) => {
        getWebInfo().then(data => {
          if (data.success) {
            commit('SET_WEB_INFO', data.data)
            resolve(data)
          } else {
            reject(data.msg)
          }
        }).catch(error => {
          reject(error)
        })
      })
    },

    login({commit, dispatch}, user) {
      return new Promise((resolve, reject) => {
        login(user.account, user.password).then(data => {
          if (data.success) {
            commit('SET_TOKEN', data.data)
            setToken(data.data)
            dispatch('getUserInfo').then(() => {
              resolve()
            }).catch(() => {
              resolve()
            })
          } else {
            reject(data.msg)
          }
        }).catch(error => {
          reject(error)
        })
      })
    },

    getUserInfo({commit, state}) {
      return new Promise((resolve, reject) => {
        getUserInfo(state.token).then(data => {
          if (data.success) {
            commit('SET_ACCOUNT',             data.data.account)
            commit('SET_NAME',                data.data.nickname)
            commit('SET_AVATAR',              data.data.avatar)
            commit('SET_ID',                  data.data.id)
            commit('SET_EMAIL',               data.data.email)
            commit('SET_SEX',                 data.data.sex)
            commit('SET_MOBILE_PHONE_NUMBER', data.data.mobilePhoneNumber)
            // 新增
            commit('SET_BIO',                 data.data.bio)
            commit('SET_BIRTHDAY',            data.data.birthday)
            commit('SET_WEBSITE',             data.data.website)
            commit('SET_SOURCE',              data.data.source)
            resolve(data)
          } else {
            dispatch('_clearUserState', null, { root: false })
            resolve(data)
          }
        }).catch(error => {
          dispatch('_clearUserState', null, { root: false })
          reject(error)
        })
      })
    },

    logout({commit, state}) {
      return new Promise((resolve, reject) => {
        logout(state.token).then(data => {
          if (data.success) {
            commit('SET_TOKEN', '')
            commit('SET_ACCOUNT', '')
            commit('SET_NAME', '')
            commit('SET_AVATAR', '')
            commit('SET_ID', '')
            commit('SET_EMAIL', '')
            commit('SET_MOBILE_PHONE_NUMBER', '')
            commit('SET_SEX', 0)
            // 新增
            commit('SET_BIO', '')
            commit('SET_BIRTHDAY', null)
            commit('SET_WEBSITE', '')
            commit('SET_SOURCE', 1)
            removeToken()
            resolve()
          }
        }).catch(error => {
          reject(error)
        })
      })
    },

    fedLogOut({commit}) {
      return new Promise(resolve => {
        commit('SET_TOKEN', '')
        commit('SET_ACCOUNT', '')
        commit('SET_NAME', '')
        commit('SET_AVATAR', '')
        commit('SET_ID', '')
        commit('SET_EMAIL', '')
        commit('SET_MOBILE_PHONE_NUMBER', '')
        commit('SET_SEX', 0)
        // 新增
        commit('SET_BIO', '')
        commit('SET_BIRTHDAY', null)
        commit('SET_WEBSITE', '')
        commit('SET_SOURCE', 1)
        removeToken()
        resolve()
      }).catch(error => {
        console.log(error)
      })
    },

    register({commit, dispatch}, user) {
      return new Promise((resolve, reject) => {
        register(user).then(data => {
          if (data.success) {
            commit('SET_TOKEN', data.data)
            setToken(data.data)
            // 注册成功后也拉一次用户信息，确保 bio/birthday/website 等都同步进来
            dispatch('getUserInfo').then(() => {
              resolve()
            }).catch(() => {
              resolve()
            })
          } else {
            reject(data.msg)
          }
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
})
