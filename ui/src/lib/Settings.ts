import { writable } from "svelte/store";
import { Bridge } from "./bridge";


export const settings=writable({
    ram:4,
    maxRam:32,
    javaArgs:""
})
export function loadSettings()
{
    Bridge.getSettings().then(v=>{
        settings.set(v);
        setTimeout(() => {
            settings.subscribe(v=>{
                Bridge.setSettings(v)
            })    
        }, 500);
        
    })
}