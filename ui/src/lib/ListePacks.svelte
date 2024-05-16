<script lang="ts">
    import { Button, Modal, Label, Input, Checkbox, Spinner, Alert, Avatar } from 'flowbite-svelte';
    import { Bridge, sendEvent } from './Bridge';
    import { createEventDispatcher, onMount } from 'svelte';
    export let formModal = false;
    var addModal=false;
    var profiles:{name:string,value:string,skin:string}[]=[];
    async function loadProfiles()
    {
        Bridge.getPacks().then(async ls=>{
            var l=[];
            for(let k in ls.list)
            {
                let v=ls.list[k];
                l.push({value:v.value,name:v.name,skin:await getSkin(v.value)})
            }
            profiles=l;
        })
    }
    async function getSkin(uuid:string)
    {
      const v=await Bridge.getPackLogo(uuid)
        if(v &&v!="none")
           return "data:image/png;base64,"+v;
        else
            return "/logo_gray.png";
    }
    onMount(async ()=>{
      Bridge.on("packsList",()=>loadProfiles())
     loadProfiles()
    })
    function removeAccount(email:string)
    {
            Bridge.removeAccount(email).then(()=>{
                loadProfiles();
                sendEvent("updateProfiles","true");
            });
    }
    var addPackCode:string;
    var alert:string;
    function addPack()
    {
      if(addPackCode && addPackCode.length>0)
      {
          Bridge.addPack(addPackCode).then(v=>{
            if(v==0)
              alert="Vous avez déja ce modpack"
            else if(v==-1)
              alert="Impossible de trouver/ d'installer ce pack"
            else
            addModal=false;
          })
      }
      else
      alert="Veuillez préciser un code"
    }
  </script>
  
  <Modal bind:open={formModal} size="xs" autoclose={false} class="w-full">
    <div class="flex flex-col space-y-6">
      <h3 class="text-xl font-medium text-gray-900 dark:text-white">Modpacks</h3>
      <ul class="my-4 space-y-3">
        {#each profiles as profil }
        <li>
            <div class="flex items-center p-3 text-base font-bold text-gray-900 bg-gray-50 rounded-lg group  dark:bg-gray-600  dark:text-white">
                <Avatar rounded  bind:src={profil.skin}/>
                <div class="flex flex-grow flex-col">
              <span class="flex-1 ms-3 whitespace-nowrap">{profil.name}</span>
              <div>
              <span class="px-2 py-0.5 flex-grow-0 ms-3 text-xs font-medium text-gray-500 bg-gray-200 rounded dark:bg-gray-700 dark:text-gray-400"> {profil.email}</span>
            </div>
                </div>
              <Button class="right-1" color=red on:click={()=>removeAccount(profil.value)}>X</Button>
              
            </div>
          </li>
        {/each}
        <li>
          <div class="flex items-center p-3 text-base font-bold ">
          <Button class="grow" color=green on:click={()=>addModal=true}>+</Button>
          </div>
        </li>
      
       
    </div>
  </Modal>
  <Modal bind:open={addModal}>
    <div class="flex flex-col space-y-6" >
      <h3 class="mb-4 text-xl font-medium text-gray-900 dark:text-white">Ajouter un modpack</h3>
        <Input type="text" placeholder="Code du Pack" bind:value={addPackCode} />
      <Button class="grow" color=green on:click={addPack}>+</Button>
      {#if alert}
      <Alert color="yellow" border>
        <span class="font-medium">Erreur!</span>
        {alert}
    </Alert>
    {/if}
  </div>
  </Modal>