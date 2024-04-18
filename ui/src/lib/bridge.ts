declare global {
    var bridge: { getProfiles: any,getSkin:any,getPacks:any,getPackLogo:any,selectProfile:any,selectPack:any,isPackLocked:any,startGame:any }
}

/**
 * Permet d'imiter de l'async avec android: La fonction android est appelé et fait tourner la requete sur un autre thread, elle appel ensuite la fonction js 'receiveAsyncResult' qui compelte la promise
 * @param func le nom de la fonction du bridge a appeler
 * @param params tout ses paramètres (l'id du callback de retour sera ajouté à la fin)
 * @returns 
 */
function AndroidPromiser<T>() {
    const requestId = requestIds;
    requestIds=requestIds+1;
    return {id:requestId,callback:new Promise<T>((resolve) => {
        callbacks[requestId] = resolve;
    })}

}

var callbacks: { [key: number]: (v: any) => void } = {};
var requestIds = 0;
export function init() {
    if (window) {
        if (!(window as any).bridge)
        {
            console.error("No Bridge Found");
            (window as any).bridge=fakeBridge
        }
        (window as any).JavaAsyncResult = receiveAsyncResult;
        (window as any).JavaEvent = receiveEvent;

    }
}

function receiveAsyncResult(id: number, response: string) {
    let callback = callbacks[id];
    if(!callback)
        return;
    if (response)
        callback(decodeURIComponent(response).replaceAll("+", " ").replaceAll("&plus;", "+"));
    else
        callback(response);
    delete callbacks[id];
}
var eventCallbacks:{[key:string]:any[]}={}
function receiveEvent(event:string,response: string) {
    let callback = eventCallbacks[event];
    if(callback)
        {
            callback.forEach(v=>{
                v(decodeURIComponent(response).replaceAll("+", " ").replaceAll("&plus;", "+"));
            })
        }
}


export const Bridge= {
    async getProfiles() {
        //we need to proceed like this because the briges functions have to be called in a literal way (can't pass it to another function)
        
        let prom=AndroidPromiser<string>();
        window.bridge.getProfiles(prom.id);
        return JSON.parse(await prom.callback) as {selected?:string,list:[{uuid:string,name:string}]};
    },
    async getSkin(uuid:string )
    {
        let prom=AndroidPromiser<string>();
        window.bridge.getSkin(uuid,prom.id);
        return await prom.callback
        
    },
    async getPackLogo(pack:string )
    {
        let prom=AndroidPromiser<string>();
        window.bridge.getPackLogo(pack,prom.id);
        return await prom.callback
        
    },
    async getPacks()
    {
        let prom=AndroidPromiser<string>();
        window.bridge.getPacks(prom.id);
        return JSON.parse(await prom.callback) as {selected?:string,list:[{name:string,value:string}]}
        
    },
    async on(event:string,fn:(msg:string)=>void)
    {
        if(!eventCallbacks[event])
            eventCallbacks[event]=[];
        eventCallbacks[event].push(fn);
        
    },
    async isPackLocked()
    {
        let prom=AndroidPromiser<string>();
        window.bridge.isPackLocked(prom.id);
        return (await prom.callback)=="true";
    },
    selectProfile(uuid:string)
    {
        window.bridge.selectProfile(uuid);
    },
    selectPack(name:string)
    {
        window.bridge.selectPack(name);
    },
    startGame()
    {
        window.bridge.startGame();
    }
};
//used for testing when outside of JavaFX context
var fakeBridge = {
    getProfiles:(callback:number)=>{receiveAsyncResult(callback,'{"list":[{"name":"Diamssword","uuid":"fakuuid"},{"name":"EaXsil","uuid":"fakuuid1"}]}')},
    getPacks:(callback:number)=>{receiveAsyncResult(callback,'{"list":[{"name":"Green Resurgence","uuid":"green"},{"name":"FTB","uuid":"ftb"}]}')},
    getSkin:(uuid:string,callback:number)=>{receiveAsyncResult(callback,'iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAACQUlEQVR4Xu3QvWrTcRQG4NBBr8JdJ0EcungBTi5eh6tXIA4OCoLgZ%2F0qim2IUqsZdPZqMmhqbYlpDA6CT%26plus%3BDAwbi9wwPhz8k5v%2FcdXLt8cfE%2FXTp3puS8nO9ynwZ%26plus%3BWDcfJOflfJf7lAL8sG4%26plus%3BSM7L%26plus%3BS73KQX4Yd18kJyX813uUwrww7r5IDkv57vcp8H1K5uLytXNsyXn5cEuA8n5rhTgg2VgOS8PdhlYznelAB8sA8t5ebDLwHK%26plus%3BKwX4YBlYzsuDXQaW810pwEBd31%2FcKx293iod774q%2FXz3tnSyPy5NH98qpQADdRlYBpaBZWAZWAZWCjBQl4FlYBlYBpaBZWClAAN1GVgGloFlYBlYBlYKmD5Z%2FihMJpN%2FcvDsbunbo5sl93X9WJZcSQEGlgu7DCwDy31dBlYKMLBc2GVgGVju6zKwUoCB5cIuA8vAcl%26plus%3BXgZUC%2FCAX6vxwozQb7ZS8p8GpYcn3aL63V0oBHpQLZWAZWN6TgeV7ZGClAA%2FKhTKwDCzvycDyPTKwUoAH5UIZWAaW92Rg%26plus%3BR4ZWCnAD3KhLow3Sovx59J8%2F2NpcPpLyffIe0oBBpYLZWB5UAaWgeV75D2lAAPLhTKwPCgDy8DyPfKeUoCB5UIZWB6UgWVg%26plus%3BR55TynADzp4eqd0uP2gdLzzsjQbDUsnyxIqvnfVp1IKWP1DCviLgWVgGVgGloHle1ethk4BKSAF%2FJECvj68sahMt26XDrfvl47ePC%2FNRrul%26plus%3BfsPpdXAWg2dAlJACkgBKSAFpIDffgGUjXakejAkOAAAAABJRU5ErkJggg%3D%3D')}
}