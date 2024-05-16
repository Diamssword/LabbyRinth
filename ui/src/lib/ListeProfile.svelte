<script lang="ts">
    import { Button, Modal, Label, Input, Checkbox, Spinner, Alert, Avatar } from 'flowbite-svelte';
    import { Bridge, sendEvent } from './Bridge';
    import { onMount } from 'svelte';
    export let formModal = false;
    var profiles:{uuid:string,email:string,name:string,skin:string}[]=[];
    async function loadProfiles()
    {
        Bridge.getProfiles().then(async ls=>{
            var l=[];
            for(let k in ls.list)
            {
                let v=ls.list[k];
                l.push({uuid:v.uuid, email:v.email,name:v.name,skin:await getSkin(v.uuid)})
            }
            profiles=l;
        })
    }
    async function getSkin(uuid:string)
    {
      const v=await Bridge.getSkin(uuid)
            if(v &&v!="none")
           return "data:image/png;base64,"+v;
        else
            return "/steve.png";
    }
    onMount(async ()=>{
     loadProfiles()
    })
    function removeAccount(email:string)
    {
            Bridge.removeAccount(email).then(()=>{
                loadProfiles();
                sendEvent("updateProfiles","true");
            });
    }
  </script>
  
  <Modal bind:open={formModal} size="xs" autoclose={false} class="w-full">
    <div class="flex flex-col space-y-6">
      <h3 class="text-xl font-medium text-gray-900 dark:text-white">Supprimer un profile</h3>
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
              <Button class="right-1" color=red on:click={()=>removeAccount(profil.email)}>X</Button>
              
            </div>
          </li>
        {/each}
      
       
    </div>
  </Modal>