<script lang="ts">
    import { Avatar, Card, Select,Label,GradientButton,Tooltip } from "flowbite-svelte";
    import { onMount } from "svelte";
    import { Bridge } from "./Bridge";
    
    var avatar="/logo_gray.png"
    var isReady=false;
    onMount(()=>{
        Bridge.on("packsReady",(r)=>isReady=r!="true")
        Bridge.isPackLocked().then(l=>isReady=!l)
        Bridge.on("packsList",(r)=>{
            let v=JSON.parse(r)
            profiles=v.list;
            selected=v.selected;
        })
        Bridge.getPacks().then(r=>{
            profiles=r.list;
            selected=r.selected
        })
    })
    $:if(selected)
    {
        Bridge.getPackLogo(selected).then(v=>{
            if(!v || v=="none")
            avatar="/logo_gray.png"
            else
            avatar="data:image/png;base64,"+v;
        })
    }
    let selected:string|undefined;
  var profiles:{value:string,name:string}[] = [];

</script>
<div class="space-y-4 w-4/4">

    <Card horizontal class="space-x-1 md:p-4 bg-gray-600 border-gray-800 w-full">
        
        <Avatar rounded src={avatar} class="h-14 w-14 self-center bg-transparent"/>
      <Select items={profiles} disabled={!isReady} bind:value={selected} placeholder="" class="h-12 self-center" on:change={()=>{}} ></Select>
    
    </Card>
    
  </div>

