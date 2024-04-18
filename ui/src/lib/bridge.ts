declare global {
    var bridge: { getProfiles: any,getSkin:any }
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
            console.error("No Bridge Found");
        (window as any).AndroidAsyncResult = receiveAsyncResult;

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
export default {
    async getProfiles() {
        //we need to proceed like this because the briges functions have to be called in a literal way (can't pass it to another function)
        
        let prom=AndroidPromiser<string>();
        window.bridge.getProfiles(prom.id);
        return JSON.parse(await prom.callback);
    },
    async getSkin(uuid:string )
    {
        let prom=AndroidPromiser<string>();
        window.bridge.getSkin(uuid,prom.id);
        return await prom.callback
        
    }
};


var fakeBridge = {

}