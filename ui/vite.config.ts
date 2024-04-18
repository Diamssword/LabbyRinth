import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'
import zipPack from "vite-plugin-zip-pack";
import fs from 'fs';

// https://vitejs.dev/config/
export default defineConfig({
  build:{},
  plugins: [svelte(),zipPack({
    outDir:"../assets/view",

  })],
});
function modifyIndexSrcPlugin() {
  return {
    name: 'modifyIndexSrcPlugin', // this name will show up in warnings and errors
    configResolved(config) {
      //this.indexDir = config.build.outDir + "/index.html";
    },
    writeBundle(options, bundle) {
      
    
      for (let ind in bundle) {
      
          var dir = options.dir+"/"+bundle[ind].fileName;
          if(typeof bundle[ind].source == "string")
          {        
            console.log("Patch des src et href pour "+ind+"...")   
          bundle[ind].source=bundle[ind].source.replace(new RegExp("src=\"/", 'g'),"src=\"").replace(new RegExp("href=\"/", 'g'),"href=\"");
         fs.writeFileSync(dir,bundle[ind].source);
          }
          
      }
    }
  };
}
