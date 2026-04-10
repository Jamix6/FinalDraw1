package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class CharacterSelectScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    // Profile slots (2 columns × 5 rows = 10 slots)
    private static final int SLOTS_PER_ROW = 2;
    private static final int TOTAL_ROWS    = 5;
    private static final int TOTAL_SLOTS   = Core.MAX_PROFILES;

    private Array<Rectangle> slotBounds;
    private int hoveredSlot  = -1;
    private int selectedSlot = -1;

    // Global action buttons (bottom of screen)
    private Rectangle deleteButtonBounds;
    private Rectangle loadButtonBounds;
    private Rectangle backButtonBounds;

    // Delete confirmation
    private boolean showDeleteConfirm = false;
    private int     slotToDelete      = -1;
    private Rectangle deleteConfirmPanelBounds;
    private Rectangle deleteConfirmYesBounds;
    private Rectangle deleteConfirmNoBounds;

    // ── Palette ──────────────────────────────────────────────────────────────
    // Background layers
    private static final Color BG_DEEP      = new Color(0.05f, 0.00f, 0.00f, 1f);
    private static final Color BG_MID       = new Color(0.10f, 0.01f, 0.01f, 1f);

    // Slot states
    private static final Color SLOT_NORMAL  = new Color(0.04f, 0.00f, 0.00f, 0.88f);
    private static final Color SLOT_HOVER   = new Color(0.10f, 0.02f, 0.00f, 0.92f);
    private static final Color SLOT_SEL     = new Color(0.16f, 0.08f, 0.00f, 0.95f);

    // Border colours
    private static final Color BORDER_NORM  = new Color(0.50f, 0.15f, 0.08f, 0.45f);
    private static final Color BORDER_HOVER = new Color(0.80f, 0.55f, 0.10f, 0.55f);
    private static final Color BORDER_SEL   = new Color(0.96f, 0.78f, 0.26f, 1.00f);

    // Text colours
    private static final Color GOLD         = new Color(0.96f, 0.78f, 0.26f, 1.00f);
    private static final Color GOLD_DIM     = new Color(0.96f, 0.78f, 0.26f, 0.55f);
    private static final Color META_COLOR   = new Color(1.00f, 1.00f, 1.00f, 0.85f);
    private static final Color LABEL_COLOR  = new Color(1.00f, 1.00f, 1.00f, 0.75f);
    private static final Color EMPTY_LABEL  = new Color(1.00f, 1.00f, 1.00f, 0.35f);
    private static final Color EMPTY_HINT   = new Color(1.00f, 1.00f, 1.00f, 0.28f);
    private static final Color SLOT_NUM_COL = new Color(1.00f, 1.00f, 1.00f, 0.40f);

    // Button colours
    private static final Color BTN_BACK_COL      = new Color(1.00f, 1.00f, 1.00f, 0.60f);
    private static final Color BTN_BACK_HOVER     = new Color(1.00f, 1.00f, 1.00f, 1.00f);
    private static final Color BTN_BACK_BG_HOVER  = new Color(1.00f, 1.00f, 1.00f, 0.05f);
    private static final Color BTN_BACK_BORDER     = new Color(1.00f, 1.00f, 1.00f, 0.22f);
    private static final Color BTN_BACK_BORD_HOV   = new Color(1.00f, 1.00f, 1.00f, 0.50f);

    private static final Color BTN_DEL_COL        = new Color(0.86f, 0.31f, 0.24f, 0.70f);
    private static final Color BTN_DEL_HOVER       = new Color(0.88f, 0.31f, 0.25f, 1.00f);
    private static final Color BTN_DEL_BG_HOVER    = new Color(0.86f, 0.31f, 0.24f, 0.06f);
    private static final Color BTN_DEL_BORDER      = new Color(0.86f, 0.31f, 0.24f, 0.25f);
    private static final Color BTN_DEL_BORD_HOV    = new Color(0.86f, 0.31f, 0.24f, 0.60f);

    private static final Color BTN_LOAD_COL        = GOLD;
    private static final Color BTN_LOAD_BG_NORM    = new Color(0.96f, 0.78f, 0.26f, 0.06f);
    private static final Color BTN_LOAD_BG_HOVER   = new Color(0.96f, 0.78f, 0.26f, 0.14f);
    private static final Color BTN_LOAD_BORDER     = new Color(0.96f, 0.78f, 0.26f, 0.40f);
    private static final Color BTN_LOAD_BORD_HOV   = GOLD;

    private static final Color DISABLED_TEXT   = new Color(1f, 1f, 1f, 0.20f);
    private static final Color DISABLED_BORDER = new Color(1f, 1f, 1f, 0.08f);
    private static final Color DISABLED_BG     = new Color(0f, 0f, 0f, 0f);

    // Confirm panel
    private static final Color CONFIRM_BG     = new Color(0.055f, 0.000f, 0.000f, 1.00f);
    private static final Color CONFIRM_BORDER = new Color(0.86f,  0.24f,  0.16f, 0.50f);
    private static final Color CONFIRM_TITLE  = new Color(0.88f,  0.25f,  0.19f, 1.00f);
    private static final Color CONFIRM_SUB    = new Color(1.00f,  1.00f,  1.00f, 0.40f);
    private static final Color OVERLAY_COL    = new Color(0.00f,  0.00f,  0.00f, 0.78f);

    // Divider gem / ornament
    private static final Color GEM_COLOR      = new Color(0.96f, 0.78f, 0.26f, 0.60f);
    private static final Color DIV_LINE       = new Color(0.96f, 0.78f, 0.26f, 0.30f);

    // ─────────────────────────────────────────────────────────────────────────

    public CharacterSelectScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch  = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();
        updateButtonPositions();
        game.playMenuMusic();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void updateButtonPositions() {
        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        // ── Action-button row metrics ─────────────────────────────────────────
        float btnW      = 140f;
        float btnH      = 40f;
        float actionRow = 60f;       // height of the action-button row (including margins)
        float rowY      = (actionRow - btnH) / 2f;   // vertical centre of that row

        // ── Grid area: everything above the action row ────────────────────────
        float titleH    = 80f;       // space for title + ornament
        float gridTop   = H - titleH;
        float gridBot   = actionRow;
        float availH    = gridTop - gridBot;

        float slotW   = 400f;
        float hGap    = 40f;
        float totalGW = SLOTS_PER_ROW * slotW + (SLOTS_PER_ROW - 1) * hGap;
        float gridX   = (W - totalGW) / 2f;

        // Distribute slot height evenly across available vertical space
        float vGap   = 12f;
        float slotH  = (availH - (TOTAL_ROWS - 1) * vGap) / TOTAL_ROWS;
        slotH = Math.min(slotH, 110f);   // cap so they don't look too tall on large screens

        // Re-centre vertically if capped
        float usedH  = TOTAL_ROWS * slotH + (TOTAL_ROWS - 1) * vGap;
        float startY = gridBot + (availH - usedH) / 2f + usedH - slotH;  // top of first row (libgdx Y-up)

        slotBounds = new Array<>(TOTAL_SLOTS);
        for (int row = 0; row < TOTAL_ROWS; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int index = row * SLOTS_PER_ROW + col;
                if (index >= TOTAL_SLOTS) break;
                float x = gridX + col * (slotW + hGap);
                float y = startY - row * (slotH + vGap);
                slotBounds.add(new Rectangle(x, y, slotW, slotH));
            }
        }

        // ── Action buttons ────────────────────────────────────────────────────
        deleteButtonBounds = new Rectangle(40f,              rowY, btnW, btnH);
        loadButtonBounds   = new Rectangle(W - 40f - btnW,  rowY, btnW, btnH);
        backButtonBounds   = new Rectangle((W - btnW) / 2f, rowY, btnW, btnH);

        // ── Delete-confirm panel ──────────────────────────────────────────────
        float panelW = 380f, panelH = 190f;
        deleteConfirmPanelBounds = new Rectangle(
            (W - panelW) / 2f, (H - panelH) / 2f, panelW, panelH);

        float cbW = 130f, cbH = 38f, cbGap = 14f;
        float panelCX = deleteConfirmPanelBounds.x + panelW / 2f;
        deleteConfirmYesBounds = new Rectangle(
            panelCX - cbW - cbGap / 2f,
            deleteConfirmPanelBounds.y + 34f, cbW, cbH);
        deleteConfirmNoBounds  = new Rectangle(
            panelCX + cbGap / 2f,
            deleteConfirmPanelBounds.y + 34f, cbW, cbH);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(BG_DEEP.r, BG_DEEP.g, BG_DEEP.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        // ── Mouse ─────────────────────────────────────────────────────────────
        float mouseX = Gdx.input.getX();
        float mouseY = H - Gdx.input.getY();

        hoveredSlot = -1;
        for (int i = 0; i < slotBounds.size; i++) {
            if (slotBounds.get(i).contains(mouseX, mouseY)) {
                hoveredSlot = i;
                break;
            }
        }

        batch.begin();
        // Use only Background.png for background
        batch.draw(game.backgroundStatic, 0, 0, W, H);

        // ── Title ─────────────────────────────────────────────────────────────
        game.titleFont.setColor(GOLD);
        String title = "SELECT CHARACTER";
        layout.setText(game.titleFont, title);
        float titleX = (W - layout.width) / 2f;
        float titleY = H - 28f;
        game.titleFont.draw(batch, title, titleX, titleY);
        batch.end();

        // ── Draw Slots using ShapeRenderer ────────────────────────────────────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        
        // Fill pass
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            Rectangle r = slotBounds.get(i);
            boolean isSelected = (i == selectedSlot);
            
            if (isSelected) {
                // Yellow with opacity if selected
                shapeRenderer.setColor(1f, 1f, 0f, 0.3f);
                drawRoundedRect(shapeRenderer, r.x, r.y, r.width, r.height, 10f);
            } else {
                // Default slot background (dark with some opacity)
                shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
                drawRoundedRect(shapeRenderer, r.x, r.y, r.width, r.height, 10f);
            }
        }
        shapeRenderer.end();

        // Outline pass
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3.0f);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            Rectangle r = slotBounds.get(i);
            boolean isSelected = (i == selectedSlot);
            
            if (isSelected) {
                shapeRenderer.setColor(Color.YELLOW);
            } else {
                shapeRenderer.setColor(Color.BLACK);
            }
            drawRoundedRectOutline(shapeRenderer, r.x, r.y, r.width, r.height, 10f);
        }
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        // Ornamental divider line + gem below title
        float divY    = H - 52f;
        float gemSize = 6f;
        float gemHalf = gemSize / 2f;
        float lineY   = divY + gemHalf;

        batch.setColor(DIV_LINE);
        // Left line
        batch.draw(game.backgroundRectangle, W * 0.08f, lineY, (W / 2f - W * 0.08f - gemHalf - 6f), 1f);
        // Right line
        batch.draw(game.backgroundRectangle, W / 2f + gemHalf + 6f, lineY, (W * 0.92f - W / 2f - gemHalf - 6f), 1f);
        // Diamond gem
        batch.setColor(GEM_COLOR);
        batch.draw(game.backgroundRectangle, W / 2f - gemHalf, divY, gemSize, gemSize);
        batch.setColor(Color.WHITE);

        // ── Slot list ─────────────────────────────────────────────────────────
        Array<Core.SaveProfile> profiles = game.getAllProfiles();

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            Rectangle r          = slotBounds.get(i);
            boolean   isSelected = (i == selectedSlot);

            Core.SaveProfile profile      = i < profiles.size ? profiles.get(i) : null;
            boolean          profileExists = profile != null && profile.exists();

            // Content
            if (profileExists) {
                drawProfileSlot(r, profile, i, isSelected);
            } else {
                boolean isHovered = (i == hoveredSlot);
                drawEmptySlot(r, i, isHovered);
            }
        }

        // ── Action buttons ────────────────────────────────────────────────────
        boolean hasValidSelection     = selectedSlot >= 0 && selectedSlot < TOTAL_SLOTS;
        boolean selectedSlotHasProfile = false;
        if (hasValidSelection) {
            Core.SaveProfile sp = selectedSlot < profiles.size ? profiles.get(selectedSlot) : null;
            selectedSlotHasProfile = sp != null && sp.exists();
        }

        boolean deleteHovered = deleteButtonBounds.contains(mouseX, mouseY);
        boolean loadHovered   = loadButtonBounds.contains(mouseX, mouseY);
        boolean backHovered   = backButtonBounds.contains(mouseX, mouseY);

        drawDeleteButton("DELETE", deleteButtonBounds, deleteHovered, selectedSlotHasProfile);
        drawLoadButton  ("LOAD",   loadButtonBounds,   loadHovered,   selectedSlotHasProfile);
        drawBackButton  ("BACK",   backButtonBounds,   backHovered);

        // ── Delete confirmation ───────────────────────────────────────────────
        if (showDeleteConfirm) {
            drawDeleteConfirmation(mouseX, mouseY);
        }

        batch.end();

        // ── Input handling ────────────────────────────────────────────────────
        if (!showDeleteConfirm && Gdx.input.justTouched()) {
            if (backButtonBounds.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                game.setScreen(new Splash());
                return;
            }
            if (loadButtonBounds.contains(mouseX, mouseY) && selectedSlotHasProfile) {
                game.playButtonSfx();
                game.setCurrentProfileSlot(selectedSlot + 1);
                game.setScreen(new MenuScreen(game));
                return;
            }
            if (deleteButtonBounds.contains(mouseX, mouseY) && selectedSlotHasProfile) {
                game.playButtonSfx();
                slotToDelete = selectedSlot + 1;
                showDeleteConfirm = true;
                return;
            }
            for (int i = 0; i < TOTAL_SLOTS; i++) {
                if (slotBounds.get(i).contains(mouseX, mouseY)) {
                    Core.SaveProfile p    = i < profiles.size ? profiles.get(i) : null;
                    boolean          exists = p != null && p.exists();
                    game.playButtonSfx();
                    if (exists) {
                        selectedSlot = (selectedSlot == i) ? -1 : i;  // toggle
                    } else {
                        game.setScreen(new CharacterCreationScreen(game, i + 1));
                        return;
                    }
                    break;
                }
            }
            // Click outside all slots → deselect
            boolean onSlot = false;
            for (Rectangle sr : slotBounds) { if (sr.contains(mouseX, mouseY)) { onSlot = true; break; } }
            if (!onSlot && !deleteButtonBounds.contains(mouseX, mouseY)
                        && !loadButtonBounds.contains(mouseX, mouseY)
                        && !backButtonBounds.contains(mouseX, mouseY)) {
                selectedSlot = -1;
            }
        }

        if (showDeleteConfirm && Gdx.input.justTouched()) {
            if (deleteConfirmYesBounds.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                if (slotToDelete > 0) {
                    game.deleteProfile(slotToDelete);
                    selectedSlot = -1;
                }
                showDeleteConfirm = false;
                slotToDelete = -1;
                return;
            }
            if (deleteConfirmNoBounds.contains(mouseX, mouseY) ||
                !deleteConfirmPanelBounds.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                showDeleteConfirm = false;
                slotToDelete = -1;
            }
        }
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    /** Draw a 1-pixel-thick border around a rectangle. */
    private void drawBorder(Rectangle r, float t) {
        batch.draw(game.backgroundRectangle, r.x,             r.y,              r.width, t);
        batch.draw(game.backgroundRectangle, r.x,             r.y + r.height - t, r.width, t);
        batch.draw(game.backgroundRectangle, r.x,             r.y,              t, r.height);
        batch.draw(game.backgroundRectangle, r.x + r.width - t, r.y,            t, r.height);
    }

    private void drawProfileSlot(Rectangle r, Core.SaveProfile profile, int idx, boolean selected) {
        float pad  = 14f;
        float topY = r.y + r.height - pad;

        // Slot number (top-left, tiny)
        game.bodyFont.setColor(SLOT_NUM_COL);
        game.bodyFont.draw(batch, "SLOT " + (idx + 1), r.x + pad, topY);

        // "SELECTED" badge (top-right) when selected
        if (selected) {
            game.bodyFont.setColor(GOLD_DIM);
            String badge = "SELECTED";
            layout.setText(game.bodyFont, badge);
            game.bodyFont.draw(batch, badge, r.x + r.width - pad - layout.width, topY);
        }

        // Character name — centre-left, smaller and brighter for contrast
        float nameY = r.y + r.height * 0.58f;
        game.bodyFont.setColor(Color.WHITE);
        float maxW       = r.width - 2f * pad;
        String display   = profile.name;
        layout.setText(game.bodyFont, display);
        if (layout.width > maxW) {
            for (int k = display.length() - 1; k > 0; k--) {
                display = profile.name.substring(0, k) + "…";
                layout.setText(game.bodyFont, display);
                if (layout.width <= maxW) break;
            }
        }
        game.bodyFont.draw(batch, display, r.x + pad, nameY);

        // Meta row: Last played only
        float metaY = r.y + pad + 12f;
        game.bodyFont.setColor(LABEL_COLOR);
        game.bodyFont.draw(batch, "Last", r.x + pad, metaY);
        game.bodyFont.setColor(META_COLOR);
        layout.setText(game.bodyFont, "Last");
        game.bodyFont.draw(batch, profile.getFormattedLastPlayed(), r.x + pad + layout.width + 6f, metaY);

        batch.setColor(Color.WHITE);
    }

    private void drawEmptySlot(Rectangle r, int idx, boolean hovered) {
        float cx = r.x + r.width  / 2f;
        float cy = r.y + r.height / 2f;

        // Slot number (top-left, same as filled slots for consistency)
        game.bodyFont.setColor(SLOT_NUM_COL);
        game.bodyFont.draw(batch, "SLOT " + (idx + 1), r.x + 14f, r.y + r.height - 14f);

        // "+" circle outline (dashed effect via 4 line segments)
        float circR = 12f;
        Color circCol = hovered ? GOLD_DIM : new Color(1f, 1f, 1f, 0.15f);
        batch.setColor(circCol);
        // Top, bottom, left, right arcs approximated as short rectangles
        batch.draw(game.backgroundRectangle, cx - circR, cy + circR - 1f, circR * 2f, 1f);
        batch.draw(game.backgroundRectangle, cx - circR, cy - circR,       circR * 2f, 1f);
        batch.draw(game.backgroundRectangle, cx - circR, cy - circR,       1f, circR * 2f);
        batch.draw(game.backgroundRectangle, cx + circR - 1f, cy - circR,  1f, circR * 2f);
        // Plus sign
        batch.draw(game.backgroundRectangle, cx - 6f, cy - 0.5f, 12f, 1f);
        batch.draw(game.backgroundRectangle, cx - 0.5f, cy - 6f, 1f, 12f);

        // Labels
        game.bodyFont.setColor(EMPTY_LABEL);
        String label = "EMPTY SLOT";
        layout.setText(game.bodyFont, label);
        game.bodyFont.draw(batch, label, cx - layout.width / 2f, cy + layout.height / 2f + 10f);

        game.bodyFont.setColor(EMPTY_HINT);
        game.bodyFont.getData().setScale(0.8f); // Make text smaller
        String hint = "Click to create character";
        layout.setText(game.bodyFont, hint);
        game.bodyFont.draw(batch, hint, cx - layout.width / 2f, cy - layout.height / 2f - 10f);
        game.bodyFont.getData().setScale(1.0f); // Reset scale

        batch.setColor(Color.WHITE);
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    private void drawBackButton(String text, Rectangle b, boolean hovered) {
        // Background
        batch.setColor(hovered ? BTN_BACK_BG_HOVER : new Color(0,0,0,0));
        batch.draw(game.backgroundRectangle, b.x, b.y, b.width, b.height);
        // Border
        batch.setColor(hovered ? BTN_BACK_BORD_HOV : BTN_BACK_BORDER);
        drawBorder(b, 1f);
        // Text
        game.bodyFont.setColor(hovered ? BTN_BACK_HOVER : BTN_BACK_COL);
        layout.setText(game.bodyFont, text);
        game.bodyFont.draw(batch, text,
            b.x + (b.width  - layout.width)  / 2f,
            b.y + (b.height + layout.height) / 2f);
        batch.setColor(Color.WHITE);
    }

    private void drawDeleteButton(String text, Rectangle b, boolean hovered, boolean enabled) {
        if (!enabled) { drawDisabledButton(text, b); return; }
        batch.setColor(hovered ? BTN_DEL_BG_HOVER : new Color(0,0,0,0));
        batch.draw(game.backgroundRectangle, b.x, b.y, b.width, b.height);
        batch.setColor(hovered ? BTN_DEL_BORD_HOV : BTN_DEL_BORDER);
        drawBorder(b, 1f);
        game.bodyFont.setColor(hovered ? BTN_DEL_HOVER : BTN_DEL_COL);
        layout.setText(game.bodyFont, text);
        game.bodyFont.draw(batch, text,
            b.x + (b.width  - layout.width)  / 2f,
            b.y + (b.height + layout.height) / 2f);
        batch.setColor(Color.WHITE);
    }

    private void drawLoadButton(String text, Rectangle b, boolean hovered, boolean enabled) {
        if (!enabled) { drawDisabledButton(text, b); return; }
        batch.setColor(hovered ? BTN_LOAD_BG_HOVER : BTN_LOAD_BG_NORM);
        batch.draw(game.backgroundRectangle, b.x, b.y, b.width, b.height);
        batch.setColor(hovered ? BTN_LOAD_BORD_HOV : BTN_LOAD_BORDER);
        drawBorder(b, 1f);
        game.bodyFont.setColor(BTN_LOAD_COL);
        layout.setText(game.bodyFont, text);
        game.bodyFont.draw(batch, text,
            b.x + (b.width  - layout.width)  / 2f,
            b.y + (b.height + layout.height) / 2f);
        batch.setColor(Color.WHITE);
    }

    private void drawDisabledButton(String text, Rectangle b) {
        batch.setColor(DISABLED_BG);
        batch.draw(game.backgroundRectangle, b.x, b.y, b.width, b.height);
        batch.setColor(DISABLED_BORDER);
        drawBorder(b, 1f);
        game.bodyFont.setColor(DISABLED_TEXT);
        layout.setText(game.bodyFont, text);
        game.bodyFont.draw(batch, text,
            b.x + (b.width  - layout.width)  / 2f,
            b.y + (b.height + layout.height) / 2f);
        batch.setColor(Color.WHITE);
    }

    // ── Delete confirmation overlay ───────────────────────────────────────────

    private void drawDeleteConfirmation(float mouseX, float mouseY) {
        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        // Full-screen dark overlay
        batch.setColor(OVERLAY_COL);
        batch.draw(game.backgroundRectangle, 0, 0, W, H);
        batch.setColor(Color.WHITE);

        // Panel fill
        Rectangle p = deleteConfirmPanelBounds;
        batch.setColor(CONFIRM_BG);
        batch.draw(game.backgroundRectangle, p.x, p.y, p.width, p.height);

        // Panel border (red)
        batch.setColor(CONFIRM_BORDER);
        drawBorder(p, 1.5f);
        batch.setColor(Color.WHITE);

        // "DELETE CHARACTER?" title
        game.titleFont.setColor(CONFIRM_TITLE);
        String warn = "DELETE CHARACTER?";
        layout.setText(game.titleFont, warn);
        game.titleFont.draw(batch, warn,
            p.x + (p.width - layout.width) / 2f,
            p.y + p.height - 26f);

        // Subtitle
        game.bodyFont.setColor(CONFIRM_SUB);
        String sub = "This action cannot be undone.";
        layout.setText(game.bodyFont, sub);
        game.bodyFont.draw(batch, sub,
            p.x + (p.width - layout.width) / 2f,
            p.y + p.height - 68f);

        // Yes / No buttons
        boolean yesHovered = deleteConfirmYesBounds.contains(mouseX, mouseY);
        boolean noHovered  = deleteConfirmNoBounds.contains(mouseX, mouseY);
        drawDeleteButton("YES, DELETE", deleteConfirmYesBounds, yesHovered, true);
        drawBackButton  ("CANCEL",      deleteConfirmNoBounds,  noHovered);
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    private void drawRoundedRect(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        renderer.rect(x + radius, y, width - 2 * radius, height);
        renderer.rect(x, y + radius, radius, height - 2 * radius);
        renderer.rect(x + width - radius, y + radius, radius, height - 2 * radius);
        renderer.arc(x + radius, y + radius, radius, 180, 90);
        renderer.arc(x + width - radius, y + radius, radius, 270, 90);
        renderer.arc(x + width - radius, y + height - radius, radius, 0, 90);
        renderer.arc(x + radius, y + height - radius, radius, 90, 90);
    }

    private void drawRoundedRectOutline(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        renderer.line(x + radius, y, x + width - radius, y);
        renderer.line(x + radius, y + height, x + width - radius, y + height);
        renderer.line(x, y + radius, x, y + height - radius);
        renderer.line(x + width, y + radius, x + width, y + height - radius);
        drawArcOnly(renderer, x + radius, y + radius, radius, 180, 90);
        drawArcOnly(renderer, x + width - radius, y + radius, radius, 270, 90);
        drawArcOnly(renderer, x + width - radius, y + height - radius, radius, 0, 90);
        drawArcOnly(renderer, x + radius, y + height - radius, radius, 90, 90);
    }

    private void drawArcOnly(ShapeRenderer renderer, float x, float y, float radius, float start, float degrees) {
        int segments = 20;
        float step = degrees / segments;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) Math.toRadians(start + i * step);
            float angle2 = (float) Math.toRadians(start + (i + 1) * step);
            renderer.line(
                x + (float) Math.cos(angle1) * radius,
                y + (float) Math.sin(angle1) * radius,
                x + (float) Math.cos(angle2) * radius,
                y + (float) Math.sin(angle2) * radius
            );
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) updateButtonPositions();
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
