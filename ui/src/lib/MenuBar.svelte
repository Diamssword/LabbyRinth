<script lang="ts">
  import { Drawer, Button, CloseButton, Sidebar, SidebarBrand, SidebarCta, SidebarDropdownItem, SidebarDropdownWrapper, SidebarGroup, SidebarItem, SidebarWrapper, Label, Tooltip, Range, Textarea } from 'flowbite-svelte';
  import Cog from 'flowbite-svelte-icons/CogOutline.svelte';
  import { sineIn } from 'svelte/easing';
    import { loadSettings, settings } from './Settings';
    import { onMount } from 'svelte';
    import { Bridge } from './Bridge';
  let hidden2 = true;
  let spanClass = 'flex-1 ms-3 whitespace-nowrap';
  
  //setTimeout(() => hidden2=false, 100);
  let transitionParams = {
    x: -320,
    duration: 200,
    easing: sineIn
  };
  onMount(()=>{
    loadSettings()
  })
  </script>
<div class="text-center">
  <Button pill class="rounded-l-lg absolute left-0 top-3" on:click={() => (hidden2 = false)}><Cog/></Button>
  <Tooltip>Options</Tooltip>
</div>
<Drawer backdrop={false} transitionType="fly" {transitionParams} bind:hidden={hidden2} id="sidebard">
  <div class="flex items-center">
    <h5 id="drawer-navigation-label-3" class="text-base font-semibold text-gray-500 uppercase dark:text-gray-400">Paramètres</h5>
    <CloseButton on:click={() => (hidden2 = true)} class="mb-4 dark:text-white" />
  </div>
  <Sidebar>
    <SidebarWrapper divClass="overflow-y-auto py-4 px-3 rounded dark:bg-gray-800">
      <SidebarGroup>
        <Label>RAM</Label>
        <div class="flex items-center gap-2 mb-10">
        <Range min="2" max={$settings.maxRam} bind:value={$settings.ram} />
        <Label>{$settings.ram}G</Label>
        </div>
        <Label>Arguments Java</Label>
        <Textarea bind:value={$settings.javaArgs} />
        <hr class="border-spacing-1 border-gray-700 mt-5 mb-5 ">
        <div class="flex center  justify-center items-center">
          <Button class="justify-center" on:click={()=>Bridge.openFolderOrUrl("root")}>Dossier du jeux</Button>
        </div>
     
      </SidebarGroup>
    </SidebarWrapper>
  </Sidebar>
</Drawer>