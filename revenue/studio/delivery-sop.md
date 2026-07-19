# Delivery SOP — how each order gets produced

The production pipeline for the AI Visual Studio. Goal: consistent, premium output in
minutes, with a fixed credit budget per order so margin stays ~100%.

## Model choice (from images_models_list)
- **Nano Banana Pro** (`imagen-nano-banana-2`) — primary. Best for product/brand
  composition, reference-guided edits, virtual staging, brand consistency, final assets.
- **Nano Banana 2 Flash** (`imagen-nano-banana-2-flash`) — faster/cheaper drafts &
  variations before committing to a 2K final.
- **GPT-2** (`gpt-2`) — when the asset needs readable text/typography (ad creatives with
  copy, promo banners).
- **Recraft V4.1** (`recraft-v4-1`) — fast first-draft text-to-image, no references.

Cost anchor: 2K premium image = **75 credits**. Draft at 1K/flash to iterate, upscale the
winner only. Budget per €59 order ≈ 750–1000 credits.

## A. E-commerce product-in-scene
1. Client sends the product photo (or a marketplace/store URL). Upload it as a `reference`
   with `type: image` (or `product` if a reusable library asset).
2. Draft 3–4 scene directions with Nano Banana Flash (1K) — e.g. marble kitchen, sunlit
   linen, minimalist studio, outdoor lifestyle.
3. Client/we pick the direction → render finals at 2K, 4:5 (feed) + 1:1 + 9:16 (story/ads).
4. `images_upscale` only if the client needs print/hi-res.
5. Deliver clean files + a 1-line usage note.

**Prompt recipe:** `"<product> placed on <surface/scene>, <lighting>, photorealistic
commercial product photography, shallow depth of field, natural shadows, high detail, no
text"` + product image reference. Keep the product's shape/label faithful — that's the
whole job; verify the label reads correctly before delivery.

## B. Real-estate virtual staging
1. Client sends the empty-room photo → upload as `image` reference.
2. Nano Banana Pro edit: furnish in a named style (Scandinavian, modern, warm minimal),
   keep architecture/windows/flooring unchanged.
3. Render 1–2 style options at 2K, 3:2.
4. Deliver with the required "virtually staged" disclosure line (legal/ethical — never
   imply the furniture is physically there).

**Prompt recipe:** `"furnish this empty <room> in <style>, realistic furniture and decor,
preserve existing walls windows floor and layout, natural daylight, real-estate listing
photography"` + room reference.

## C. Video ad clip (Brand set / add-on)
1. Take the best still → `video_generate` with the image as a keyframe (pass the creation
   `identifier`, never a webUrl).
2. Short 3–6s motion loop (slow push-in, product turn). Run `video_plan` first.
3. Optional `video_upscale`. Deliver MP4.

## Quality gate before any delivery
- [ ] Product/label/architecture is faithful — no warped logos, no invented text
- [ ] No AI artifacts (extra fingers, melted edges, impossible reflections)
- [ ] Correct aspect ratios for the client's channel
- [ ] Virtual staging carries the disclosure line
- [ ] Files named clearly, deduped, delivered in the promised format
- [ ] Human has approved the batch before it goes to the client (live-send gate)

## Credit ledger (keep updated)
| Date | Order | Credits used | Revenue € |
|---|---|---|---|
| | | | |
