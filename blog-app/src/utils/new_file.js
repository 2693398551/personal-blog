let style = document.createElement('style')
let graySelector = 'gray-filter'
style.setAttribute('type', 'text/css')

style.textContent = `.${graySelector}{
  -webkit-filter: grayscale(100%);
  -moz-filter: grayscale(100%);
  -ms-filter: grayscale(100%);
  -o-filter: grayscale(100%);
  filter: grayscale(100%);
  filter: gray;
  filter: progid:dximagetransform.microsoft.basicimage(grayscale=1);
}`
document.head.appendChild(style)

let root = document.querySelector('html')
let btn = document.querySelector('#set-gray')
btn && btn.addEventListener('click', () => {
  setAllGray()
}, false)

function toggleClassName(el,name){
  if (el.className.indexOf(name) > -1) {
    el.className = el.className.replace(name, '').trim()
	console.log("1")
	console.log(el)
  } else {
    el.className = [el.className, name].join(' ')
	console.log("2")
	console.log(el)
  }
  
}

export function setAllGray() {
  toggleClassName(root,graySelector)
}

 

