import  fastify  from 'fastify';
import * as semver from "semver-parser";
const app = fastify();
import * as path from 'path';
import * as fs  from 'fs';

  
                        //a voir si on m'est pas en place le version sorting directement coté launcher (infra serveur beaucoup plus légère)
                        //https://stackoverflow.com/questions/41198379/sorting-version-numbers
const port=process.env.port||3000;
app.listen({ port }).then(() => {
    console.log('Server running at http://localhost:'+port);
});
var packs = {};
var lastRefresh = 0;

function readFolders() {
    packs = {};
    if (fs.existsSync("packs")) {
        fs.readdirSync("packs").forEach(fold => {
            if (fs.statSync("packs/" + fold).isDirectory()) {
                const vers=[];
                fs.readdirSync("packs/" + fold).forEach(file => {
                    
                    if (fs.statSync("packs/" + fold + "/" + file).isFile()) {
                        const ver = file.substring(0, file.lastIndexOf("."))
                        if (semver.isValidSemVer(ver))
                               vers.push({version:semver.parseSemVer(ver).version,path:fold + "/" + file});
                    }
                })
                if(vers.length>0)
                {
                    vers.sort((a,b)=>semver.compareSemVer(b.version,a.version))
                    packs[fold]={versions:vers,latest:vers[0]};
                }
            }
        })
    }
}

app.get("/", async (req, res) => {
   if(new Date().getTime()> lastRefresh+5000)
   {
        lastRefresh=new Date().getTime();
        readFolders();
   }
   res.send(packs);
});

app.get("/packs/:pack/:version",(req,res)=>{
    if(req.params.pack  && req.params.version)
    {
        const name="packs/"+req.params.pack+"/"+req.params.version
       if(fs.existsSync(name) && fs.statSync(name).isFile())
       {
        return res.header("Content-Length",fs.statSync(name).size).header('Content-disposition', 'attachment; filename='+req.params.pack+"-"+req.params.version).type(req.params.version.substring(0,req.params.version.lastIndexOf("."))).send(fs.createReadStream(name))
       }
    }
    res.code(404).send("File not found")
})