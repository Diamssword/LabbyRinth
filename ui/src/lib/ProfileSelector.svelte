<script lang="ts">
    import { Avatar, Card, Select,Label,GradientButton,Tooltip } from "flowbite-svelte";
    import { createEventDispatcher, onMount } from "svelte";
    import { Bridge } from "./bridge";
    import AddAccount from "./AddAccount.svelte";
    var avatar="/steve.png"
    
    const disp=createEventDispatcher();
    function loadProfiles()
    {
        Bridge.getProfiles().then(ls=>{
            profiles=ls.list.map(v=>{return {value:v.uuid,name:v.name}})
            if(ls.selected)
            {
                selected=ls.selected;
            }
        })
    }
    onMount(()=>{
     loadProfiles()
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
    var addAcount=false
</script>
<div class="space-y-4 w-4/4 profil">

    <Card horizontal class="space-x-1 md:p-4 bg-gray-600 border-gray-800 w-full">
        <Avatar rounded src={avatar} class="h-14 w-14 self-center bg-transparent"/>
      <Select items={profiles} bind:value={selected} placeholder={profiles.length>0?"":"Ajoutez un profile..."} class="h-12 self-center" on:change={()=>{
          Bridge.selectProfile(selected);
      }}></Select>
    <GradientButton color=green shadow class="h-12 self-center" on:click={()=>addAcount=true}>+</GradientButton>
    </Card>
    
  </div>
  <AddAccount bind:formModal={addAcount} on:refresh={()=>{loadProfiles(); disp("refresh");}} />

<style>
    .profil{
        margin-bottom: 16px;
    }
</style>