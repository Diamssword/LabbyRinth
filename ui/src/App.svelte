<script lang="ts">
  import {Bridge, init} from './lib/Bridge';  
  import { Button, GradientButton, Progressbar } from 'flowbite-svelte';

  import MenuBar from './lib/MenuBar.svelte';
    import { onMount } from 'svelte';
    import ProfileSelector from './lib/ProfileSelector.svelte';
    import PackSelector from './lib/PackSelector.svelte';
    
    var isGameReady=false;
    var progress=0;
    var status="";
    var ready=false;
    var users={}
  onMount(()=>{
    setTimeout(() => {
      init(); 
      ready=true;
      Bridge.on("packsReady",(r)=>isGameReady=r!="true")
      Bridge.isPackLocked().then(l=>isGameReady=!l)
      Bridge.on("progress",(r)=>progress=parseInt(r))
      Bridge.on("status",(r)=>status=r)
    }, 500);
  })
</script>

<main class="flex h-screen">
  
  {#if ready}
      <div class="w-3/12 "><MenuBar/></div>
      <div class="w-5/12 flex flex-wrap items-end justify-center mb-44">
        <div class="flex items-center flex-col w-full justify-center gap-8">
        <div class="w-4/5">
          <div class="mb-1 font-medium dark:text-white">{status}</div>
            <Progressbar progress={progress} size="h-4" labelInside  />
          </div>
          <GradientButton disabled={!isGameReady} color="green" size="xl" shadow pill class="w-4/5 top-3/4" on:click={Bridge.startGame}>Jouer</GradientButton >
          </div>
      </div >
      <div class="w-4/12 flex flex-wrap justify-end mt-10 mr-5" ><ProfileSelector/><PackSelector/></div>
      {/if}
</main>

<style>

</style>
