const CACHE="keyveni-menu-v12";
const STATIC_FILES=["./","./index.html","./manifest.webmanifest","./assets/icon.svg"];
self.addEventListener("install",event=>event.waitUntil(caches.open(CACHE).then(cache=>cache.addAll(STATIC_FILES)).then(()=>self.skipWaiting())));
self.addEventListener("activate",event=>event.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(key=>key!==CACHE).map(key=>caches.delete(key)))).then(()=>self.clients.claim())));
self.addEventListener("fetch",event=>{
 const request=event.request;
 if(request.method!=="GET")return;
 const url=new URL(request.url);
 if(url.origin!==self.location.origin)return;
 const allowed=STATIC_FILES.some(file=>new URL(file,self.registration.scope).pathname===url.pathname);
 if(!allowed)return;
 event.respondWith(caches.match(request).then(cached=>cached||fetch(request).then(response=>{
  if(response.ok){const copy=response.clone();caches.open(CACHE).then(cache=>cache.put(request,copy))}
  return response;
 }).catch(()=>caches.match("./index.html"))));
});