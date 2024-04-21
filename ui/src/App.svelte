<script lang="ts">
  import {Bridge, init} from './lib/Bridge';  
  import { Button, GradientButton, Progressbar, Spinner } from 'flowbite-svelte';
  import MenuBar from './lib/MenuBar.svelte';
    import { onMount } from 'svelte';
    import ProfileSelector from './lib/ProfileSelector.svelte';
    import PackSelector from './lib/PackSelector.svelte';
    const bgCount=4;
    var bgCurrent=parseInt((Math.random()*4).toFixed())
    if(bgCurrent=bgCount)
      bgCurrent=0;
    setInterval(()=>{
      if(bgCurrent+1>=bgCount)
        bgCurrent=0;
      else
        bgCurrent++
    },10000)
    var isGameReady=false;
    var progress=100;
    var status="Prêt au lancement!";
    var ready=false;
  onMount(()=>{
    setTimeout(() => {
      init(); 
      ready=true;
      Bridge.on("packsReady",(r)=>isGameReady=r!="true")
      checkLocked();
      Bridge.on("progress",(r)=>progress=parseInt(r))
      Bridge.on("status",(r)=>status=r)
    }, 500);
  })
  function checkLocked()
  {
    Bridge.isPackLocked().then(l=>isGameReady=!l)
  }
</script>

<main  class="flex h-screen bg-gray-800 select-none" style='background-image: url("{"/bg/"+bgCurrent+".png"}");'>
  
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
      <div class="w-4/12 flex flex-wrap justify-end mt-10 mr-5" ><ProfileSelector on:refresh={checkLocked}/><PackSelector/></div>
      {:else}
      <div class="flex items-center flex-col w-full justify-center">
      <Spinner color=green size={20} class="text-center "/>
    </div>
      {/if}
</main>

<style>
  main{
   background-size: cover;
   /* -webkit-transition: background-image 2s ease-in-out;
    transition: background-image 2s ease-in-out; Pas de transition pour le moment avec javafx, elles devraient être dispo autour de la version 23+12*/
  }
  
</style>
