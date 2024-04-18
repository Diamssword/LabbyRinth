<script lang="ts">
    import { Avatar, Card, Select,Label,GradientButton,Tooltip } from "flowbite-svelte";
    import { onMount } from "svelte";
    import { Bridge } from "./Bridge";
    var avatar="/steve.png"
    onMount(()=>{
        Bridge.getProfiles().then(ls=>{
            profiles=ls.list.map(v=>{return {value:v.uuid,name:v.name}})
            if(ls.selected)
            {
                selected=ls.selected;
            }
        })
    })
    $:if(selected)
    {
        Bridge.getSkin(selected).then(v=>{
            if(!v || v=="none")
            avatar="/steve.png"
            else
            avatar="data:image/png;base64,"+v;
        })
    }
    let selected:string;
  var profiles:{value:string,name:string}[] = [];

</script>
<div class="space-y-4 w-4/4">

    <Card horizontal class="space-x-1 md:p-4 bg-gray-600 border-gray-800 w-full">
        
        <Avatar rounded src={avatar} class="h-14 w-14 self-center bg-transparent"/>
      <Select items={profiles} bind:value={selected} placeholder="Ajoutez un profile..." class="h-12 self-center" on:change={()=>{
          Bridge.selectProfile(selected);
      }}></Select>
    
    <GradientButton color=green shadow class="h-12 self-center">+</GradientButton>
    </Card>
    
  </div>

