<script>
  import { onMount, onDestroy, tick } from "svelte"
  import { fade, fly, scale } from "svelte/transition"
  import { flip } from "svelte/animate"
  import { setContext, getContext } from "svelte"
  import { writable, readable, derived } from "svelte/store"

  // Props: `value` is bindable from the parent, `children` renders parent snippets.
  let {
    title = "Alabaster · Svelte 5 showcase",
    items = [
      { id: 1, label: "Runes", done: false },
      { id: 2, label: "Snippets", done: true },
      { id: 3, label: "Stores", done: false }
    ],
    value = $bindable(0),
    children
  } = $props()

  // $state: reactive primitives
  let query = $state("")
  let agreed = $state(false)
  let flavour = $state("vanilla")
  let chosen = $state("b")
  let volume = $state(50)
  let scrollY = $state(0)
  let open = $state(false)
  let canvasEl = $state()
  let formEl = $state()

  // $derived for one-liners, $derived.by when a body is needed
  let filtered = $derived(items.filter((item) => item.label.toLowerCase().includes(query.toLowerCase())))
  let undone = $derived(items.filter((item) => !item.done).length)
  let summary = $derived.by(() => `${undone} of ${items.length} items still open`)

  // $effect re-runs whenever its reactive dependencies change
  $effect(() => {
    console.log(`visible: ${filtered.length}, scrollY: ${scrollY}`)
  })

  // Context pair: provided here, consumed by descendants with getContext
  setContext("theme", { accent: "#3b82f6", mode: "dark" })
  const theme = getContext("theme")

  // Stores: $count, $doubled and $clock auto-subscribe in the template
  const count = writable(0)
  const doubled = derived(count, ($count) => $count * 2)
  const clock = readable(new Date(), (set) => {
    const id = setInterval(() => set(new Date()), 1000)
    return () => clearInterval(id)
  })

  // Action invoked as `use:autoFocus`
  function autoFocus(node, enabled = true) {
    if (enabled && typeof node.focus === "function") node.focus()
    return {
      update(next) {
        enabled = next
      },
      destroy() {}
    }
  }

  // Lifecycle helpers, still supported next to runes
  onMount(async () => {
    await tick()
    console.log("mounted:", canvasEl instanceof HTMLCanvasElement)
  })
  onDestroy(() => console.log("destroyed"))

  function handleSubmit(event) {
    event.preventDefault()
    value = volume
    open = !open
  }

  // Promises for the await block
  const settings = Promise.resolve({ region: "eu-west-1", retries: 3 })
  const broken = Promise.reject(new Error("offline"))
  broken.catch(() => {})
</script>

<svelte:options runes={true} />

<svelte:head>
  <title>{title}</title>
  <meta name="description" content="Svelte 5 runes, snippets, stores and transitions.">
</svelte:head>

<svelte:window bind:scrollY onkeydown={(event) => event.key === "Escape" && (open = false)} />
<svelte:body onmouseleave={() => console.log("pointer left the body")} />

<!-- Snippets replace slots and can be rendered like components -->
{#snippet badge(text, tone = "neutral")}
  <span class="badge badge--{tone}" data-tone={tone}>{text}</span>
{/snippet}

{#snippet row(item, position)}
  {@const state = item.done ? "done" : "open"}
  <li class="row" animate:flip={{ duration: 180 }} in:fly={{ y: 8 }} out:fade={{ duration: 120 }}>
    <span class="index" aria-hidden="true">{position}.</span>
    <label>
      <input type="checkbox" bind:checked={item.done}>
      <span class:done={item.done}>{item.label}</span>
    </label>
    <span class="state" class:done={item.done}>{state}</span>
  </li>
{/snippet}

<header>
  <h1>{title}</h1>
  <p>{summary} · doubled: {$doubled} · clock: {$clock.toLocaleTimeString()}</p>
  <button type="button" onclick={() => ($count += 1)}>Increment store ({$count})</button>
</header>

<main>
  <section aria-labelledby="filters-heading">
    <h2 id="filters-heading">Bindings</h2>
    <label for="q">Search</label>
    <input id="q" type="search" placeholder="Filter items…" bind:value={query} oninput={() => (open = true)}>
    <label for="volume">Volume: <output>{volume}</output></label>
    <input id="volume" type="range" min="0" max="100" step="1" bind:value={volume}>
    <p style:color={volume > 50 ? theme.accent : "inherit"} style:--weight={volume}>Slider-tinted text</p>
  </section>

  <section aria-labelledby="list-heading">
    <h2 id="list-heading">Each, key and animate</h2>
    {#if filtered.length === 0}
      <p>Nothing matches “{query}”.</p>
    {:else if filtered.length === 1}
      <p>Exactly one match.</p>
    {:else}
      <ul>
        {#each filtered as item, index (item.id)}
          {@const position = index + 1}
          {@render row(item, position)}
        {/each}
      </ul>
    {/if}
    {@debug filtered, value}
  </section>

  {#key flavour}
    <p transition:fade={{ duration: 200 }}>Selected flavour: {flavour}</p>
  {/key}

  <section aria-labelledby="async-heading">
    <h2 id="async-heading">Await</h2>
    {#await settings}
      <p aria-busy="true" in:fly={{ y: -7 }}>Loading settings…</p>
    {:then config}
      <dl>
        <dt>Region</dt><dd>{config.region}</dd>
        <dt>Retries</dt><dd>{config.retries}</dd>
      </dl>
    {:catch error}
      <p role="alert">Settings unavailable: {error.message}</p>
    {/await}
    {#await broken}
      <p>Pending…</p>
    {:then result}
      <p>{result}</p>
    {:catch error}
      <p>Optional request failed: {error.message}</p>
    {/await}
  </section>

  <section aria-labelledby="form-heading">
    <h2 id="form-heading">Form handling</h2>
    <form bind:this={formEl} onsubmit={handleSubmit} onreset={() => (agreed = false)}>
      <fieldset>
        <legend>Preferences</legend>
        <label><input type="checkbox" bind:checked={agreed} required> I accept the terms</label>
        {#each ["vanilla", "chocolate", "pistachio"] as option (option)}
          <label>
            <input type="radio" value={option} bind:group={flavour}>
            {option}
          </label>
        {/each}
        <label for="size">Size</label>
        <select id="size" bind:value={chosen}>
          <option value="a">Small</option>
          <option value="b">Medium</option>
          <option value="c">Large</option>
        </select>
        <button type="submit" disabled={!agreed} use:autoFocus>Submit</button>
        <button type="reset" onpointerdown={() => (open = false)}>Reset</button>
      </fieldset>
    </form>
  </section>

  <section aria-labelledby="markup-heading">
    <h2 id="markup-heading">Markup and composition</h2>
    <canvas bind:this={canvasEl} width="240" height="80" role="img" aria-label="Decorative canvas"></canvas>
    <div class="note" transition:scale={{ duration: 200, start: 0.95 }}>
      {@html "<strong>Trusted</strong> HTML rendered verbatim."}
    </div>
    {#if children}
      {@render children()}
    {:else}
      <p>No children supplied by the parent.</p>
    {/if}
    <button type="button" popovertarget="sheet" aria-expanded={open}>Details</button>
  </section>
</main>

<div id="sheet" popover>
  <p>Popover content opened with <code>popovertarget</code>.</p>
</div>

<footer>
  <p>Built with Svelte 5 · accent {theme.accent}</p>
</footer>
