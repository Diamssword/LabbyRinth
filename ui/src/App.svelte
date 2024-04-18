<script lang="ts">
  import LayoutGrid, { Cell } from '@smui/layout-grid';
  import bridge,{init} from './lib/bridge';
  import MenuBar from './lib/MenuBar.svelte';
  import Button, { Label } from '@smui/button';
    import { onMount } from 'svelte';
    var ready=false;
  onMount(()=>{
setTimeout(() => {
  init();
  ready=true;
  bridge.getProfiles().then(d=>{console.log(JSON.stringify(d));
    bridge.getSkin("cd270c6a52754147a46c6c1eb21efc7c").then(console.log)
  })
  
}, 500);
  })
</script>

<main>
  
  {#if ready}
      <div class="side_menu menu-cell"><MenuBar/></div>
      <div class="side_menu button-cell">
        <div >
        <Button class="" variant="raised">
          <Label>Raised</Label>
        </Button>
      </div>
      </div>
      <div class="side_menu right-cell" >Span 4</div>
      {/if}
</main>

<style>
main{
  display: flex;
}
.menu-cell{
  width: 30%;
  height: 100vh;
}
.button-cell{
  width: 40%;
  height: 100vh;
}
.right-cell{
  width: 30%;
  height: 100vh;
}
 .side_menu {
    align-content: so;
    display: flex;
    align-items: center;
    background-color: var(--mdc-theme-secondary, #333);
    color: var(--mdc-theme-on-secondary, #fff);
  }
</style>
